#!/usr/bin/env python3
"""
Assign a `villainRange` field to every scenario in scenarios.json.

The range is a short, educational description of what villain's hand range
should look like given the (street, position, villainAction) context —
including a note about morphology (wide/tight, polar/merged/capped).

Run:
    python3 scripts/assign_villain_ranges.py

Idempotent: re-running overwrites the existing villainRange field.
"""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SCENARIOS_PATH = ROOT / "app" / "src" / "main" / "assets" / "scenarios.json"


# ---------------------------------------------------------------------------
# Canonical preflop ranges
# ---------------------------------------------------------------------------

RFI_RANGES = {
    "UTG": (
        "Tight ~14% open: 77+, ATs+, KTs+, QTs+, JTs, T9s, 98s, 87s, 76s, "
        "65s, AQo+, KQo."
    ),
    "MP": (
        "~17% open: 55+, A9s+, A5s-A4s, KTs+, QTs+, J9s+, T9s, 98s, 87s, "
        "76s, 65s, AJo+, KQo."
    ),
    "HJ": (
        "~20% open: 44+, A7s+, A5s-A2s, K9s+, Q9s+, J9s+, T8s+, 97s+, 86s+, "
        "75s+, 65s, ATo+, KJo+, QJo."
    ),
    "LJ": (
        "~20% open: 44+, A7s+, A5s-A2s, K9s+, Q9s+, J9s+, T8s+, 97s+, 86s+, "
        "75s+, 65s, ATo+, KJo+, QJo."
    ),
    "CO": (
        "~27% open: 22+, A2s+, K7s+, Q8s+, J8s+, T8s+, 97s+, 86s+, 75s+, "
        "64s+, 54s, ATo+, KTo+, QTo+, JTo."
    ),
    "BTN": (
        "~47% open: 22+, A2s+, K2s+, Q5s+, J7s+, T7s+, 96s+, 85s+, 74s+, "
        "64s+, 53s+, A2o+, K8o+, Q9o+, J9o+, T9o, 98o."
    ),
    "SB": (
        "~40% open/complete: 22+, A2s+, K5s+, Q8s+, J8s+, T8s+, 97s+, 86s+, "
        "75s+, 65s, 54s, A8o+, KTo+, QTo+, JTo."
    ),
}

POS_PCT = {
    "UTG": "14", "MP": "17", "HJ": "20", "LJ": "20",
    "CO": "27", "BTN": "47", "SB": "40",
}


THREE_BET_VS_EP = (
    "Linear 3-bet range: QQ+, AK, AQs, KQs, AJs. Mostly value — not many "
    "bluff combos vs a tight EP opener."
)

THREE_BET_VS_LP = (
    "Polar 3-bet range: QQ+, AK for value; A5s-A2s, K9s-K7s, Q9s-Q8s, 76s, "
    "65s as bluffs. Strong hands + suited playability bluffs."
)

SQUEEZE_POLAR = (
    "Polar squeeze: QQ+, AK, AQs for value; A5s-A2s, K9s-K8s, T8s, 76s, 65s "
    "as bluffs. Dead money drives bluff frequency."
)

FOUR_BET_POLAR = (
    "Polar 4-bet: QQ+, AK for value; A5s-A4s, sometimes KQs as bluffs. "
    "Blocker-heavy bluff selection."
)

FOUR_BET_POT_CALLER = (
    "Ultra-narrow 4bp calling range: JJ-QQ (mixed), AKs, AQs, sometimes AK "
    "off. Range is tight, capped below KK, and heavy on suited broadways."
)

SHORT_JAM = {
    20: (
        "Nash ~20BB jam: 77+, ATs+, KJs+, QJs, AJo+, KQo. Blockers + equity "
        "vs calling range."
    ),
    15: (
        "Nash ~15BB jam: 66+, A8s+, KTs+, QTs+, JTs, A9o+, KJo+. Tight push "
        "range that runs well vs call range."
    ),
    10: (
        "Nash ~10BB jam: 22+, A2s+, K7s+, Q8s+, J8s+, T9s, A7o+, K9o+, "
        "QTo+, JTo."
    ),
}

LIMP_RANGE = (
    "Loose-passive limp range: 22-TT, Axs, suited broadways that don't "
    "3-bet, suited gappers, occasional KQ/AJ limp-trap. Uncapped but "
    "rarely the nuts — often the second-nuts."
)

NO_VILLAIN_YET = (
    "No villain has entered yet — think about your opening range from this "
    "position and who is still left to act behind you."
)

SB_FLAT_RANGE = (
    "Capped SB flat range: 22-JJ, ATs-AQs, KJs-KQs, QJs, JTs, T9s, 98s, "
    "ATo-AQo, KJo-KQo, QJo. QQ+ and most AK 3-bet, so the top of this "
    "range is missing."
)

SB_COMPLETE_RANGE = (
    "SB complete (limp) range: wide + speculative — suited hands, weak "
    "aces, small pairs, suited gappers. Strong hands raise, so this "
    "range is capped."
)

BB_DEFEND_VS_BTN = (
    "~55% BB defense: 22-JJ, A2s+, K5s+, Q7s+, J7s+, T7s+, 96s+, 85s+, "
    "74s+, 64s+, 53s+, A2o-AQo, K9o+, Q9o+, J9o+, T9o."
)

BB_DEFEND_VS_CO = (
    "~42% BB defense: 22-JJ, A2s-AQs, K7s+, Q8s+, J8s+, T8s+, 97s+, 86s+, "
    "76s, 65s, 54s, A8o+, KTo+, QTo+, JTo."
)

BB_DEFEND_VS_EP = (
    "~28% BB defense: 22-TT, A2s-AJs, K9s-KQs, Q9s-QJs, J9s-JTs, T9s, 98s, "
    "87s, 76s, A9o-AJo, KJo-KQo. Tighter defense vs an EP raiser."
)


# ---------------------------------------------------------------------------
# Canonical postflop ranges (range morphology, not exhaustive)
# ---------------------------------------------------------------------------

PFR_EP = (
    "Villain is EP/MP preflop raiser — tight range (~14-17%): 77+, ATs+, "
    "KTs+, QTs+, JTs, T9s, AQo+, KQo."
)

PFR_LP = (
    "Villain is CO/BTN preflop raiser — wide range (~27-47%): 22+, most "
    "suited aces, suited broadways, suited connectors, broadway off-suits."
)

PFR_CBET_RANGE = (
    "Villain's c-bet range is close to their preflop open (60-75% of it). "
    "Contains top pair, overpairs, sets, and most draws/overcards/backdoors "
    "as bluffs. Range is polar on drier boards, more merged on wet boards."
)

BB_FLAT_RANGE_POSTFLOP = (
    "Villain defended BB — wide and uncapped: suited connectors (T9s-54s), "
    "suited gappers, most suited aces, pocket pairs 22-JJ, broadway off-"
    "suits. Strong hands 3-bet, so this range has no premiums."
)

SRP_CALL_RANGE = (
    "Flop calling range: top pair (weak-to-mid kicker), middle pair, pocket "
    "pairs below top card, gutshots, open-enders, flush draws. Capped — "
    "strong hands raise."
)

THREE_BP_CALL_IP = (
    "Villain flatted a 3-bet in position — capped: TT-JJ (mixed), AJs-AQs, "
    "KQs, AKo (mixed). No QQ+/AK hard-value (4-bet). Broadway + medium-pair."
)

THREE_BP_CALL_BB = (
    "Villain defended 3-bet from BB: 66-JJ, AJs-AQs, KTs-KQs, QJs, JTs, "
    "T9s, 98s, ATo-AQo, KQo. Wide but capped."
)

FOUR_BP_CALL_RANGE = (
    "Villain called a 4-bet — ultra-narrow: JJ-QQ, AQs, AJs (mixed), AKs/"
    "AKo. Range is tight, capped below KK, heavy on suited broadways."
)

OOP_CHECK_RANGE = (
    "OOP check range: weak made hands (3rd pair, ace-high floats), gutshots, "
    "showdown hands happy to check-call. Capped below top pair."
)

CHECK_RAISE_POLAR = (
    "Polar check-raise range: sets, two pair, top pair strong kicker (value) "
    "+ strong draws (open-enders, big flush draws, combo draws) as "
    "semi-bluffs. Balanced at 2:1 value:bluff on most textures."
)

DONK_BET_RANGE = (
    "Donk-lead range: weak-to-medium made hands wanting protection (middle "
    "pair, bottom two, weak top pair) + the occasional trap (set/straight). "
    "Missing the nuts (would check-raise) and pure air (would check-fold)."
)

TURN_CHECK_RANGE = (
    "Turn check range (flatted flop, checks turn): medium made hands "
    "planning to check-call, weak top pair, busted draws. Nuts rarely "
    "check — they protect this range with occasional slowplays."
)

TURN_BET_RANGE = (
    "Turn betting range: value (top pair+ kicker, overpairs, sets, two "
    "pair) + draws that picked up equity. Polar on drawy turns, merged on "
    "brick turns."
)

TURN_CHECK_BACK_RANGE = (
    "IP check-back range (capped): marginal showdown hands — middle pair, "
    "ace-high with showdown, weak top pair, busted turn draws. Strong value "
    "bets; pure air bluffs; this range is what's left."
)

TURN_BARREL_CALL_RANGE = (
    "Two-street calling range: top pair decent kicker, overpairs that "
    "called down, weak two pair, turn-improved draws, and occasional "
    "floats that haven't given up. Capped — raises represent nut combos."
)

RIVER_CHECK_RANGE = (
    "Capped river check range: showdown hands (2nd/3rd pair, ace-high), "
    "busted draws, and give-up bluffs. Strong value rarely checks the "
    "river — it protects with a small slowplay frequency."
)

RIVER_BET_POLAR = (
    "Polarized river bet range: top pair+ kicker and better (straights, "
    "flushes, sets, two pair) + matching-frequency bluffs with blockers. "
    "Big sizing = polar."
)

RIVER_BET_MERGED = (
    "Merged river bet range: thin value (weak top pair, overpairs, some "
    "pairs needing protection) + a smaller share of bluffs. Smaller "
    "sizing = thinner value-heavy range."
)

RIVER_JAM_POLAR = (
    "Polar river jam: straights, flushes, full houses, sets + balanced "
    "bluff combos with blockers. Very little medium-strength value — "
    "expect either the nuts or nothing."
)

MULTIWAY_CALLERS = (
    "Multiway callers' ranges skew to pocket pairs (set-mining), suited "
    "connectors (implied odds), and suited aces. Strong made hands are "
    "rare — cold-calling multiway prefers big-implied hands."
)


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

POS_RE = r"(UTG\+?\d*|UTG|MP|HJ|LJ|CO|BTN|SB|BB)"


def _opener_class(pos: str) -> str:
    return "EP" if pos.upper() in {"UTG", "MP", "HJ", "LJ"} else "LP"


# ---------------------------------------------------------------------------
# Preflop classifier
# ---------------------------------------------------------------------------

def _preflop_range(scenario: dict, va: str, va_low: str) -> str:
    # 1. "Folded to you" / "No action yet" / "SB completes"
    if "folded to you" in va_low or "folds to you" in va_low or "no action yet" in va_low:
        return NO_VILLAIN_YET
    if re.search(r"\bsb completes\b", va_low):
        return SB_COMPLETE_RANGE

    # 2. Jam spots
    m_jam = re.search(rf"{POS_RE}\s+jams?\s+(\d+)bb", va, re.IGNORECASE)
    if m_jam:
        stack = int(m_jam.group(2))
        if stack >= 18:
            return SHORT_JAM[20]
        if stack >= 13:
            return SHORT_JAM[15]
        return SHORT_JAM[10]

    # 3. 4-bet facing hero's 3-bet
    if ("4-bet" in va_low or "4bets" in va_low or "4-bets" in va_low):
        return FOUR_BET_POLAR

    # 4. Three actions: opener + 3-bettor (+ optional caller)
    m3 = re.search(
        rf"{POS_RE}\s+opens[^A-Za-z]*.*?{POS_RE}\s+3-?bets", va, re.IGNORECASE
    )
    if m3:
        opener = m3.group(1).upper()
        three_bettor = m3.group(2).upper()
        rng = THREE_BET_VS_EP if _opener_class(opener) == "EP" else THREE_BET_VS_LP
        return (
            f"{opener} open + {three_bettor} 3-bet. " + rng
        )

    # 5. Limp action
    if "limps" in va_low or "limp" in va_low:
        return LIMP_RANGE

    # 6. Squeeze-style: "X opens, Y calls"
    m_sq = re.search(
        rf"{POS_RE}\s+opens[^A-Za-z]*.*?{POS_RE}\s+calls", va, re.IGNORECASE
    )
    if m_sq:
        opener = m_sq.group(1).upper()
        caller = m_sq.group(2).upper()
        pct = POS_PCT.get(opener, "25")
        if caller == "SB":
            return f"{opener} open (~{pct}%) + SB cold-flat. {SB_FLAT_RANGE}"
        # Multiple callers: "X opens, Y calls, Z calls" — treat as multiway/cold-call
        if va_low.count("calls") >= 2:
            return (
                f"{opener} open (~{pct}%) + multiple cold-callers. Callers "
                "hold loose suited hands, pocket pairs, and suited broadways "
                "— capped, since QQ+/AK 3-bet."
            )
        return (
            f"{opener} open (~{pct}%) + {caller} flat — loose suited hands, "
            "pocket pairs, suited broadways. Capped because strong hands "
            "3-bet."
        )

    # 7. Hero opened, villain 3-bets
    m_open3 = re.search(
        rf"you opened.*{POS_RE}\s+3-?bets", va, re.IGNORECASE
    )
    if m_open3:
        three_bettor = m_open3.group(1).upper()
        if _opener_class(three_bettor) == "LP":
            return f"{three_bettor} 3-bet. {THREE_BET_VS_LP}"
        return f"{three_bettor} 3-bet. {THREE_BET_VS_EP}"

    # 8. Bare RFI: "UTG opens 2.5BB" / "BTN opens 2BB"
    m_rfi = re.search(rf"^{POS_RE}\s+opens", va, re.IGNORECASE)
    if m_rfi:
        opener = m_rfi.group(1).upper()
        return RFI_RANGES.get(opener, RFI_RANGES["BTN"])

    # Generic fallback
    return (
        "Villain range depends on the exact action. Rule of thumb: EP ~14%, "
        "MP ~17%, CO ~27%, BTN ~47%, SB ~40%, BB widest when defending."
    )


# ---------------------------------------------------------------------------
# Postflop classifier
# ---------------------------------------------------------------------------

def _postflop_range(scenario: dict, va: str, va_low: str, street: str) -> str:
    # Most specific first.

    # 4-bet pot: "3-bet pot, you 4-bet and they called"
    if "4-bet" in va_low and ("3-bet pot" in va_low or "called" in va_low):
        return FOUR_BP_CALL_RANGE

    # 3-bet pot from hero's generator: "Hero 3-bet preflop, villain called IP"
    if ("hero 3-bet" in va_low or "3-bet preflop" in va_low) and "called ip" in va_low:
        return THREE_BP_CALL_IP
    if "hero 3-bet ip" in va_low and "called from bb" in va_low:
        return THREE_BP_CALL_BB
    if "(3-bet caller)" in va_low or ("3-bet pot" in va_low and "you" not in va_low):
        return THREE_BP_CALL_IP

    # Check-raise action (villain is the check-raiser) — polar
    if "check-raise" in va_low or "check-raises" in va_low:
        return CHECK_RAISE_POLAR

    # Donk bet (villain leads OOP into hero as PFR)
    if "donk" in va_low:
        return DONK_BET_RANGE

    # Multiway pots
    if "3-way" in va_low or "multiway" in va_low:
        return MULTIWAY_CALLERS

    # PFR c-bet explicit tag: "(preflop raiser) bets"
    if "(preflop raiser)" in va_low:
        return PFR_CBET_RANGE

    # Hero was the PFR on BTN: "Hero called BTN open preflop" (villain is BTN PFR)
    if "called btn open" in va_low:
        return PFR_LP
    # Generator tag: "BB called hero's preflop open" (villain is BB, defended)
    if "bb called" in va_low and "hero" in va_low and "preflop open" in va_low:
        return BB_FLAT_RANGE_POSTFLOP

    # Villain checked OOP in SRP (BB or SB with context in parens)
    if re.search(r"\bbb\b.*\bchecks\b", va_low) and "vs your" in va_low:
        return BB_FLAT_RANGE_POSTFLOP
    if re.search(r"\b(defender|caller)\)?\s+checks\b", va_low):
        return BB_FLAT_RANGE_POSTFLOP

    # Hero checked turn / called, checks river
    if street == "RIVER" and ("checks river" in va_low or "checks" in va_low.split(" ")[-1:]):
        # The capped river check range applies broadly
        return RIVER_CHECK_RANGE

    # "Villain called flop c-bet" + "checked turn"
    if "called flop c-bet" in va_low and "checked turn" in va_low:
        return TURN_CHECK_RANGE
    if "called flop c-bet" in va_low:
        return SRP_CALL_RANGE

    # Villain check-called earlier streets — do this BEFORE the bet regex,
    # because text like "BB check-called flop bet, now checks again" would
    # otherwise match the bet pattern.
    if "check-called" in va_low:
        if street == "RIVER":
            return RIVER_CHECK_RANGE
        return TURN_CHECK_RANGE

    # "Villain c-bet" on FLOP — villain was PFR
    if "c-bet" in va_low and "called" not in va_low and "check-raise" not in va_low:
        if street == "FLOP":
            return PFR_CBET_RANGE
        # After c-bet on earlier street, now a later street
        return SRP_CALL_RANGE

    # "Checked back flop" / "Both checked flop"
    if "checked back flop" in va_low or "both checked flop" in va_low:
        return TURN_CHECK_BACK_RANGE

    # "Checked back turn" / river action after turn check-back
    if "checked back turn" in va_low or (street == "RIVER" and "action checks" in va_low):
        return TURN_CHECK_BACK_RANGE

    # Hand-written turn/river bet phrasings like "Villain bets 12BB (75%)"
    m_bet = re.search(r"(?:villain|bb|sb|utg|mp|co|btn|hj|lj)\s+bets?\s+\d+bb\s*\(\s*(\d+)%\s*\)",
                      va_low)
    if m_bet:
        pct = int(m_bet.group(1))
        if street == "RIVER":
            return RIVER_BET_POLAR if pct >= 85 else RIVER_BET_MERGED
        if street == "TURN":
            return TURN_BET_RANGE

    # Pot-size or bigger bet
    if "pot" in va_low and "bets" in va_low:
        return RIVER_BET_POLAR if street == "RIVER" else TURN_BET_RANGE

    # Generic "Villain bets XBB"
    if re.search(r"(villain|bb|sb|utg|mp|co|btn|hj|lj).*\bbets?\b", va_low):
        if street == "RIVER":
            return RIVER_BET_MERGED
        if street == "TURN":
            return TURN_BET_RANGE
        return PFR_CBET_RANGE

    # River jam
    if street == "RIVER" and ("jam" in va_low or "all-in" in va_low or "all in" in va_low):
        return RIVER_JAM_POLAR

    # "Checks to hero" / "Checks to you"
    if "checks to hero" in va_low or "checks to you" in va_low:
        return OOP_CHECK_RANGE

    # "River action on you" — villain has called through
    if "river action" in va_low:
        return TURN_BARREL_CALL_RANGE

    # "checked turn" alone on TURN street
    if street == "TURN" and "checked turn" in va_low:
        return TURN_CHECK_RANGE
    if street == "TURN" and "checks" in va_low:
        return TURN_CHECK_RANGE

    # Bare "Villain checks" on RIVER
    if street == "RIVER" and ("checks" in va_low or "action checks" in va_low):
        return RIVER_CHECK_RANGE

    # "BB checks to you (3-way pot)"
    if "checks" in va_low and ("3-way" in va_low or "way pot" in va_low):
        return MULTIWAY_CALLERS

    # "Hero opened, CO and BB called"
    if "hero opened" in va_low or "you opened" in va_low:
        return SRP_CALL_RANGE

    # Fallback by street
    return {
        "FLOP": SRP_CALL_RANGE,
        "TURN": TURN_CHECK_RANGE,
        "RIVER": RIVER_CHECK_RANGE,
    }.get(street, SRP_CALL_RANGE)


# ---------------------------------------------------------------------------
# Dispatcher
# ---------------------------------------------------------------------------

def classify(scenario: dict) -> str:
    va = scenario["villainAction"]
    va_low = va.lower()
    street = scenario["street"]
    if street == "PREFLOP":
        return _preflop_range(scenario, va, va_low)
    return _postflop_range(scenario, va, va_low, street)


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

def main() -> int:
    scenarios = json.loads(SCENARIOS_PATH.read_text())
    for s in scenarios:
        s["villainRange"] = classify(s)
    SCENARIOS_PATH.write_text(
        json.dumps(scenarios, indent=2, ensure_ascii=False) + "\n"
    )
    print(f"assigned villainRange to {len(scenarios)} scenarios")
    return 0


if __name__ == "__main__":
    sys.exit(main())
