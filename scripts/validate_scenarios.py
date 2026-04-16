#!/usr/bin/env python3
"""
Scenario validator for poker-trainer.

Catches logical inconsistencies between a scenario's hand, board, street, and
its `explanation` text. Used both as a one-shot audit script and as the source
of truth for our Kotlin unit tests (which mirror the same checks).

Run:
    python3 scripts/validate_scenarios.py
    python3 scripts/validate_scenarios.py --strict   # exit 1 on any issue

Output format:
    ID  STREET  HAND  BOARD  -> issues
"""

from __future__ import annotations

import argparse
import json
import re
import sys
from itertools import combinations
from pathlib import Path
from typing import Iterable

# ---------------------------------------------------------------------------
# Card parsing
# ---------------------------------------------------------------------------

SUIT_GLYPHS = {"♠": "s", "♥": "h", "♦": "d", "♣": "c"}
SUIT_LETTERS = {"s", "h", "d", "c", "S", "H", "D", "C"}
RANK_ORDER = "23456789TJQKA"
RANK_VAL = {r: i + 2 for i, r in enumerate(RANK_ORDER)}  # 2..14


def parse_card(card: str) -> tuple[str, str] | None:
    """Return (rank_letter, suit_letter) or None if unparseable.

    Accepts "A♠", "AS", "As", "10♥" (we normalise 10→T)."""
    if not card:
        return None
    c = card.strip().replace("10", "T")
    if len(c) < 2:
        return None
    suit_char = c[-1]
    rank_part = c[:-1]
    if suit_char in SUIT_GLYPHS:
        suit = SUIT_GLYPHS[suit_char]
    elif suit_char in SUIT_LETTERS:
        suit = suit_char.lower()
    else:
        return None
    rank = rank_part.upper()
    if rank not in RANK_VAL:
        return None
    return (rank, suit)


def parse_hand(hand_str: str) -> list[tuple[str, str]]:
    """Parse a 2-card hand like 'A♠K♠' or 'AsKs' into [(rank,suit), ...]."""
    if not hand_str:
        return []
    s = hand_str.strip().replace("10", "T")
    cards: list[tuple[str, str]] = []
    i = 0
    while i < len(s):
        # rank may be 1 char (digit/letter), then suit (1 char glyph or letter)
        if i + 1 >= len(s):
            break
        rank = s[i].upper()
        suit_char = s[i + 1]
        if rank in RANK_VAL and (suit_char in SUIT_GLYPHS or suit_char in SUIT_LETTERS):
            suit = SUIT_GLYPHS.get(suit_char, suit_char.lower())
            cards.append((rank, suit))
            i += 2
        else:
            i += 1
    return cards


# ---------------------------------------------------------------------------
# Hand evaluation helpers
# ---------------------------------------------------------------------------

def is_straight(rank_vals: Iterable[int]) -> bool:
    """True if the 5 distinct rank values form a straight (incl. wheel A-5)."""
    s = sorted(set(rank_vals))
    if len(s) < 5:
        return False
    # check any 5-window
    for i in range(len(s) - 4):
        window = s[i : i + 5]
        if window[-1] - window[0] == 4:
            return True
    # wheel: A,2,3,4,5
    if {14, 2, 3, 4, 5}.issubset(set(s)):
        return True
    return False


def best_straight_high(rank_vals: Iterable[int]) -> int | None:
    """High card of the highest straight present in `rank_vals`, or None."""
    s = sorted(set(rank_vals))
    if len(s) < 5:
        # check wheel only if A present (handled below)
        if {14, 2, 3, 4, 5}.issubset(set(rank_vals)):
            return 5
        return None
    best: int | None = None
    for i in range(len(s) - 4):
        window = s[i : i + 5]
        if window[-1] - window[0] == 4:
            best = window[-1]
    if best is None and {14, 2, 3, 4, 5}.issubset(set(s)):
        return 5
    return best


def has_flush(suits: Iterable[str]) -> str | None:
    """Return the suit of any 5+ card flush, else None."""
    counts: dict[str, int] = {}
    for su in suits:
        counts[su] = counts.get(su, 0) + 1
    for su, n in counts.items():
        if n >= 5:
            return su
    return None


def best_possible_hand_for_two_unknown(board: list[tuple[str, str]]) -> str:
    """
    Coarse "is the nuts available?" classifier returning one of:
    'STRAIGHT_FLUSH', 'QUADS', 'FULL_HOUSE', 'FLUSH', 'STRAIGHT',
    'SET'/'TRIPS', 'TWO_PAIR', 'PAIR', 'HIGH'.

    Computed by trying every 2-card combination from the remaining 47 cards
    against this board. Used for "are claims of 'nuts' plausible?" checks.
    """
    deck = [
        (r, s)
        for r in RANK_ORDER
        for s in "shdc"
        if (r, s) not in board
    ]
    best_rank = 0  # 0 = high
    rank_names = [
        "HIGH",
        "PAIR",
        "TWO_PAIR",
        "TRIPS",
        "STRAIGHT",
        "FLUSH",
        "FULL_HOUSE",
        "QUADS",
        "STRAIGHT_FLUSH",
    ]

    for c1, c2 in combinations(deck, 2):
        cards = board + [c1, c2]
        ranks_v = [RANK_VAL[r] for r, _ in cards]
        suits = [s for _, s in cards]
        # compute hand class
        flush_suit = has_flush(suits)
        is_st = is_straight(ranks_v)
        # straight flush
        sf = False
        if flush_suit:
            flush_ranks = [RANK_VAL[r] for r, s in cards if s == flush_suit]
            sf = is_straight(flush_ranks)
        rank_counts: dict[int, int] = {}
        for v in ranks_v:
            rank_counts[v] = rank_counts.get(v, 0) + 1
        counts_sorted = sorted(rank_counts.values(), reverse=True)

        if sf:
            cls = 8
        elif counts_sorted[0] == 4:
            cls = 7
        elif counts_sorted[0] == 3 and counts_sorted[1] >= 2:
            cls = 6
        elif flush_suit:
            cls = 5
        elif is_st:
            cls = 4
        elif counts_sorted[0] == 3:
            cls = 3
        elif counts_sorted[0] == 2 and counts_sorted[1] == 2:
            cls = 2
        elif counts_sorted[0] == 2:
            cls = 1
        else:
            cls = 0

        if cls > best_rank:
            best_rank = cls

    return rank_names[best_rank]


# ---------------------------------------------------------------------------
# Validator
# ---------------------------------------------------------------------------

# Words that imply specific board structures (only checked on flops/turns).
RAINBOW_PATTERNS = [
    re.compile(r"\brainbow\b", re.I),
    re.compile(r"\bthree[- ]?suit", re.I),
]
MONOTONE_PATTERNS = [re.compile(r"\bmonotone\b", re.I)]
TWO_TONE_PATTERNS = [re.compile(r"\btwo[- ]?tone\b", re.I)]
# Only flag "paired board" when used as a present-tense statement of fact,
# not "paired board could beat you" (future possibility).
PAIRED_BOARD_PATTERNS = [
    re.compile(
        r"\b(this is a |on (a|this) |the )paired board\b|"
        r"\bboard is paired\b|board pairs (and|now)",
        re.I,
    )
]

# Specific hand-class claims that can be cross-checked.
CLAIM_BACKDOOR_FLUSH = re.compile(r"backdoor[- ]flush", re.I)

# Hero must explicitly say "you/I/we/hero have a flush draw" — not just
# "board has flush draw possibilities" (that's villain-side).
CLAIM_HERO_FLUSH_DRAW = re.compile(
    r"\b(you have|i have|we have|hero has|having)\s+(a\s+)?(nut\s+|backdoor[- ]?)?flush draw",
    re.I,
)

# Hero claims hand X = absolute nuts. Match unqualified "nuts" (no "nut flush"
# or "nut straight" — those are tested separately, since they can be true
# claims even when an even-better hand class exists).
CLAIM_HERO_HAS_NUTS = re.compile(
    r"(?i)("
    # Anchor 1: "second nuts" anywhere — strong claim
    r"\bsecond\s+nuts\b"
    # Anchor 2: hero pronoun + verb + (the) nuts (NOT 'nut flush'/'nut straight')
    r"|\b(you|we|i|hero|your|my|hero's)\s+"
    r"(have|has|hold|holds|holding|made|got|is\s+on|are\s+on)\s+"
    r"(the\s+)?(second\s+)?nuts\b"
    # Anchor 3: "your/my X is the (second) nuts"
    r"|\b(your|my|hero's)\s+\w+(\s+\w+){0,3}\s+is\s+"
    r"(the\s+)?(second\s+)?nuts\b"
    r")"
)

# Specific straight call-outs like "AT has the nuts with Broadway".
# Case-insensitive — explanations sometimes write "broadway" lowercase in
# parentheses.
NAMED_STRAIGHT_REGEXES = {
    "Broadway": re.compile(r"\bbroadway\b", re.I),
    "wheel": re.compile(r"\bwheel\b", re.I),
}

# Negation context — if these words appear within ~40 chars of a
# Broadway/wheel mention, the explanation is correctly stating the straight
# is NOT achievable, so we should not flag.
NEGATION_NEAR = re.compile(
    r"(?i)\b(impossible|not\s+possible|cannot|can't|isn't|is\s+not|no\s+one\s+can|"
    r"would\s+require|would\s+need|requires?\s+a|needs?\s+a|missing|without|"
    r"never|no\s+\w+\s+(on\s+the\s+)?board)\b"
)


def hand_combos_with_board(board: list[tuple[str, str]]) -> list[list[tuple[str, str]]]:
    """All 7-card combinations using every possible 2-card hole (47C2 = 1081)."""
    deck = [
        (r, s)
        for r in RANK_ORDER
        for s in "shdc"
        if (r, s) not in board
    ]
    return [board + [c1, c2] for c1, c2 in combinations(deck, 2)]


def can_make_named_straight(board: list[tuple[str, str]], name: str) -> bool:
    """Is it physically possible for some 2-card holding to make this straight?"""
    if name == "Broadway":
        needed = {10, 11, 12, 13, 14}  # T J Q K A
    elif name == "wheel":
        needed = {14, 2, 3, 4, 5}
    else:
        return True
    board_vals = {RANK_VAL[r] for r, _ in board}
    missing = needed - board_vals
    return len(missing) <= 2  # can fit in 2 hole cards


def make_straight_high(hand: list[tuple[str, str]], board: list[tuple[str, str]]) -> int | None:
    """High card of the best straight made by `hand` + `board`, else None."""
    cards = hand + board
    return best_straight_high(RANK_VAL[r] for r, _ in cards)


def has_set(hand: list[tuple[str, str]], board: list[tuple[str, str]]) -> bool:
    """A 'set' = pocket pair that hits the board (3 of a kind using both hole cards)."""
    if len(hand) != 2:
        return False
    if hand[0][0] != hand[1][0]:
        return False
    return any(b[0] == hand[0][0] for b in board)


def has_two_pair(hand: list[tuple[str, str]], board: list[tuple[str, str]]) -> bool:
    """Two pair using at least one hole card."""
    if not hand:
        return False
    h_ranks = [r for r, _ in hand]
    b_ranks = [r for r, _ in board]
    pairs = 0
    for r in h_ranks:
        if r in b_ranks:
            pairs += 1
    if hand[0][0] == hand[1][0]:
        pairs = max(pairs, 1)
    return pairs >= 2


def validate(scenario: dict) -> list[str]:
    issues: list[str] = []
    sid = scenario.get("id", "<no-id>")
    street = scenario.get("street", "")
    hand = parse_hand(scenario.get("hand", ""))
    board_strs = scenario.get("board", []) or []
    board = []
    for b in board_strs:
        c = parse_card(b)
        if c is None:
            issues.append(f"unparseable board card '{b}'")
        else:
            board.append(c)

    if len(hand) != 2:
        issues.append(f"hand did not parse to 2 cards: '{scenario.get('hand')}'")

    # Duplicate cards check
    all_cards = hand + board
    if len(set(all_cards)) != len(all_cards):
        dups = [c for c in all_cards if all_cards.count(c) > 1]
        issues.append(f"duplicate cards: {sorted(set(dups))}")

    # Street vs board length
    expected_board = {"PREFLOP": 0, "FLOP": 3, "TURN": 4, "RIVER": 5}
    if street in expected_board:
        if len(board) != expected_board[street]:
            issues.append(
                f"street={street} expects {expected_board[street]} board cards, got {len(board)}"
            )

    explanation = scenario.get("explanation", "") or ""

    # ---- Board structure claims (only meaningful on flop/turn/river) ----
    if street in ("FLOP", "TURN", "RIVER") and len(board) >= 3:
        flop = board[:3]
        flop_suits = [s for _, s in flop]
        unique_flop_suits = len(set(flop_suits))

        if any(p.search(explanation) for p in RAINBOW_PATTERNS):
            if unique_flop_suits != 3:
                issues.append(
                    f"explanation says 'rainbow' but flop has {unique_flop_suits} suits ({flop_suits})"
                )

        if any(p.search(explanation) for p in MONOTONE_PATTERNS):
            if unique_flop_suits != 1:
                issues.append(
                    f"explanation says 'monotone' but flop has {unique_flop_suits} suits ({flop_suits})"
                )

        if any(p.search(explanation) for p in TWO_TONE_PATTERNS):
            if unique_flop_suits != 2:
                issues.append(
                    f"explanation says 'two-tone' but flop has {unique_flop_suits} suits ({flop_suits})"
                )

        if any(p.search(explanation) for p in PAIRED_BOARD_PATTERNS):
            board_ranks = [r for r, _ in board]
            if len(set(board_ranks)) == len(board_ranks):
                issues.append(
                    f"explanation says 'paired board' but board has no pair ({board_ranks})"
                )

    # ---- Backdoor flush draw must have flop with 3-of-same-suit not in hand ----
    if CLAIM_BACKDOOR_FLUSH.search(explanation) and street == "TURN" and len(board) == 4:
        # backdoor flush draw becomes a real (4-card) flush draw on the turn —
        # i.e., on the flop you had 3 of one suit between hand+flop, and the
        # turn brought the 4th. So total of one suit across hand+turn-board
        # should be exactly 4.
        all_suits = [s for _, s in hand + board]
        max_suit_count = max(all_suits.count(s) for s in "shdc") if all_suits else 0
        if max_suit_count < 4:
            issues.append(
                f"says 'backdoor flush' but turned card doesn't give 4-of-suit (max={max_suit_count})"
            )

    # ---- Hero flush-draw claim (only when text explicitly says HERO has it) ----
    if (
        CLAIM_HERO_FLUSH_DRAW.search(explanation)
        and street in ("FLOP", "TURN")
        and len(board) >= 3
    ):
        all_suits = [s for _, s in hand + board]
        max_suit_count = max(all_suits.count(s) for s in "shdc") if all_suits else 0
        if max_suit_count < 4:
            issues.append(
                f"says hero has a flush draw but only {max_suit_count} cards of any one "
                f"suit across hand+board"
            )

    # ---- Specific named straight claims ("Broadway", "wheel") ----
    # Only meaningful postflop. On preflop, "wheel"/"Broadway" refers to a
    # hand's general potential, not a made hand.
    #
    # Rule: the named straight needs `needed - board_ranks` cards from the
    # remaining unknowns. Total unknowns = 2 hole + (5 - len(board)) future
    # community cards. If `missing > unknowns`, the straight is physically
    # unreachable for anyone — that's a real explanation bug.
    if street in ("FLOP", "TURN", "RIVER"):
        future_cards_by_street = {"FLOP": 4, "TURN": 3, "RIVER": 2}
        unknowns = future_cards_by_street.get(street, 0)
        for name, regex in NAMED_STRAIGHT_REGEXES.items():
            for m in regex.finditer(explanation):
                if name == "Broadway":
                    needed = {10, 11, 12, 13, 14}
                elif name == "wheel":
                    needed = {14, 2, 3, 4, 5}
                else:
                    continue
                board_vals = {RANK_VAL[r] for r, _ in board}
                missing_from_board = needed - board_vals
                if len(missing_from_board) <= unknowns:
                    # achievable — nothing to flag
                    break
                # If a negation word appears within 60 chars of THIS mention,
                # the explanation is correctly stating the straight is NOT
                # achievable. Don't flag.
                window_start = max(0, m.start() - 60)
                window_end = min(len(explanation), m.end() + 60)
                window = explanation[window_start:window_end]
                if NEGATION_NEAR.search(window):
                    continue
                issues.append(
                    f"explanation mentions '{name}' but it is physically impossible "
                    f"on this {street.lower()} board (missing "
                    f"{sorted(missing_from_board)}; only {unknowns} more cards "
                    f"available across hand + future community)"
                )
                break  # one issue per scenario per name is enough

    # ---- "Nuts" claim: hero must actually have the best possible class ----
    if (
        CLAIM_HERO_HAS_NUTS.search(explanation)
        and street == "RIVER"
        and len(board) == 5
    ):
        # Compute hero's class and the best possible class on the board.
        nut_class = best_possible_hand_for_two_unknown(board)
        # Hero's actual class
        hero_cards = hand + board
        hero_ranks = [RANK_VAL[r] for r, _ in hero_cards]
        hero_suits = [s for _, s in hero_cards]
        flush_suit = has_flush(hero_suits)
        is_st = is_straight(hero_ranks)
        sf = False
        if flush_suit:
            flush_ranks = [RANK_VAL[r] for r, s in hero_cards if s == flush_suit]
            sf = is_straight(flush_ranks)
        rank_counts: dict[int, int] = {}
        for v in hero_ranks:
            rank_counts[v] = rank_counts.get(v, 0) + 1
        counts_sorted = sorted(rank_counts.values(), reverse=True)
        if sf:
            hero_class = "STRAIGHT_FLUSH"
        elif counts_sorted[0] == 4:
            hero_class = "QUADS"
        elif counts_sorted[0] == 3 and counts_sorted[1] >= 2:
            hero_class = "FULL_HOUSE"
        elif flush_suit:
            hero_class = "FLUSH"
        elif is_st:
            hero_class = "STRAIGHT"
        elif counts_sorted[0] == 3:
            hero_class = "TRIPS"
        elif counts_sorted[0] == 2 and counts_sorted[1] == 2:
            hero_class = "TWO_PAIR"
        elif counts_sorted[0] == 2:
            hero_class = "PAIR"
        else:
            hero_class = "HIGH"

        rank_order_classes = [
            "HIGH",
            "PAIR",
            "TWO_PAIR",
            "TRIPS",
            "STRAIGHT",
            "FLUSH",
            "FULL_HOUSE",
            "QUADS",
            "STRAIGHT_FLUSH",
        ]
        if rank_order_classes.index(hero_class) < rank_order_classes.index(nut_class):
            issues.append(
                f"says 'nuts' but hero has {hero_class} while nuts available = {nut_class}"
            )

    # ---- Set claim: must be pocket pair matching board ----
    if street in ("FLOP", "TURN", "RIVER") and len(board) >= 3:
        if re.search(r"\bflopped a set\b|\bturned a set\b|\brivered a set\b|\bI have a set\b|\bhero (has|flopped) a set\b", explanation, re.I):
            if not has_set(hand, board):
                issues.append("claims a set but hand is not pocket pair matching board")

    # ---- Two pair claim ----
    if street in ("FLOP", "TURN", "RIVER") and len(board) >= 3:
        if re.search(r"\b(flopped|turned|rivered) two pair\b", explanation, re.I):
            if not has_two_pair(hand, board):
                issues.append("claims two pair but hand+board don't make two pair")

    return issues


# ---------------------------------------------------------------------------
# Self-test: synthetic buggy scenarios the validator MUST catch.
# Run with `--self-test`. Used both for local regression and as a sanity check
# whenever the validator regexes are edited.
# ---------------------------------------------------------------------------

SELF_TEST_FIXTURES = [
    {
        "label": "Broadway impossible on river (no J on board)",
        "scenario": {
            "id": "test_broadway_river_impossible",
            "hand": "J♣T♣",
            "position": "BTN",
            "board": ["Q♥", "9♣", "3♦", "6♠", "K♦"],
            "street": "RIVER",
            "explanation": "AT has the nuts with Broadway here.",
        },
        "must_contain": "Broadway",
    },
    {
        "label": "Hero claims 'second nuts' but actually has trips on a wheel-possible board",
        "scenario": {
            "id": "test_second_nuts_false",
            "hand": "8♠8♣",
            "position": "BTN",
            "board": ["8♥", "5♠", "2♠", "Q♣", "A♥"],
            "street": "RIVER",
            "explanation": "Your set is the second nuts; bet for value.",
        },
        "must_contain": "nuts",
    },
    {
        "label": "Rainbow claim on a two-tone flop",
        "scenario": {
            "id": "test_rainbow_wrong",
            "hand": "A♠K♠",
            "position": "BTN",
            "board": ["Q♠", "7♠", "2♦"],
            "street": "FLOP",
            "explanation": "On this rainbow flop you should c-bet.",
        },
        "must_contain": "rainbow",
    },
    {
        "label": "Hero flush draw claim with only 3 of one suit",
        "scenario": {
            "id": "test_false_flush_draw",
            "hand": "A♥9♥",
            "position": "BTN",
            "board": ["A♣", "T♠", "7♣"],
            "street": "FLOP",
            "explanation": "You have a flush draw and top pair.",
        },
        "must_contain": "flush draw",
    },
    {
        "label": "Duplicate card across hand and board",
        "scenario": {
            "id": "test_duplicate",
            "hand": "A♠K♠",
            "position": "BTN",
            "board": ["A♠", "Q♥", "2♦"],
            "street": "FLOP",
            "explanation": "Top pair top kicker.",
        },
        "must_contain": "duplicate",
    },
    {
        "label": "Wrong board length for street",
        "scenario": {
            "id": "test_wrong_board",
            "hand": "A♠K♠",
            "position": "BTN",
            "board": ["A♣", "Q♥"],
            "street": "FLOP",
            "explanation": "Top pair.",
        },
        "must_contain": "expects 3",
    },
]

# Things that should NOT be flagged (false-positive guards).
SELF_TEST_NEGATIVES = [
    {
        "label": "Preflop A5s mentioning wheel potential is fine",
        "scenario": {
            "id": "test_preflop_wheel_ok",
            "hand": "A♣5♣",
            "position": "BTN",
            "board": [],
            "street": "PREFLOP",
            "explanation": "A5s plays well — nut flush and wheel straight potential.",
        },
    },
    {
        "label": "'block the nuts' is a blocker comment, not a hero claim",
        "scenario": {
            "id": "test_blocks_nuts_ok",
            "hand": "A♦5♦",
            "position": "BTN",
            "board": ["K♦", "8♦", "6♣", "T♥", "J♠"],
            "street": "RIVER",
            "explanation": "When you block the nuts, villain folds more often.",
        },
    },
    {
        "label": "Drawing to Broadway on the turn (gutshot) is achievable, no flag",
        "scenario": {
            "id": "test_broadway_draw_ok",
            "hand": "A♦K♥",
            "position": "BTN",
            "board": ["Q♠", "7♦", "2♣", "J♥"],
            "street": "TURN",
            "explanation": "You picked up a gutshot draw needing any Ten for Broadway.",
        },
    },
    {
        "label": "Negation context: 'Broadway is impossible'",
        "scenario": {
            "id": "test_broadway_negated_ok",
            "hand": "J♦T♦",
            "position": "SB",
            "board": ["9♠", "8♣", "2♥", "Q♦"],
            "street": "TURN",
            "explanation": "Broadway is impossible here because no Ace is on the board.",
        },
    },
]


def run_self_test() -> int:
    failures = 0
    for fx in SELF_TEST_FIXTURES:
        issues = validate(fx["scenario"])
        text = " | ".join(issues)
        if fx["must_contain"].lower() not in text.lower():
            failures += 1
            print(f"FAIL  positive  '{fx['label']}'")
            print(f"      expected issue containing: {fx['must_contain']!r}")
            print(f"      actual issues: {issues}")
        else:
            print(f"PASS  positive  '{fx['label']}'")
    for fx in SELF_TEST_NEGATIVES:
        issues = validate(fx["scenario"])
        if issues:
            failures += 1
            print(f"FAIL  negative  '{fx['label']}'")
            print(f"      expected NO issues, got: {issues}")
        else:
            print(f"PASS  negative  '{fx['label']}'")
    print(f"\nSelf-test: {failures} failure(s).")
    return 0 if failures == 0 else 1


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--path",
        default="app/src/main/assets/scenarios.json",
        help="Path to scenarios JSON",
    )
    parser.add_argument("--strict", action="store_true", help="Exit 1 on any issue")
    parser.add_argument("--id", default=None, help="Validate only this scenario id")
    parser.add_argument(
        "--self-test",
        action="store_true",
        help="Run validator self-tests on synthetic fixtures",
    )
    args = parser.parse_args()

    if args.self_test:
        return run_self_test()

    p = Path(args.path)
    if not p.exists():
        print(f"ERROR: not found: {p}", file=sys.stderr)
        return 2

    data = json.loads(p.read_text())
    total = len(data)
    bad = 0
    for s in data:
        if args.id and s.get("id") != args.id:
            continue
        issues = validate(s)
        if issues:
            bad += 1
            print(
                f"\n{s.get('id', '?'):14}  street={s.get('street'):7}  "
                f"hand={s.get('hand'):<8}  board={s.get('board')}"
            )
            for i in issues:
                print(f"   - {i}")

    print(f"\n{total - bad}/{total} scenarios passed validation; {bad} have issues.")
    if bad and args.strict:
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
