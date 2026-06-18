# European layout only (0) + exact casino layout overlay.

Implementation approach (so it matches your betIndex map precisely):

1. Keep the existing “0 + 1–36 grid” as the visual base.
2. Add an overlay layer that places transparent clickable hotspots:
   - Vertical splits (between rows): betIndex 201..211 etc (and the other vertical split series you defined)
   - Horizontal splits (between columns): betIndex 212.. etc
   - Corners at 4-number intersections: betIndex 400..422
   - Streets above each row: betIndex 500..511
   - Six-lines between two adjacent streets: betIndex 600..610
   - Trio (0-1-2, 0-2-3): betIndex 301, 300
   - Zero splits: 0-1 (248), 0-2 (224), 0-3 (200), plus 0-2-3 and 0-1-2 already as trio
3. Each hotspot click calls placeChipOnBetIndex(<mapped index>) so it integrates with:
   - Undo (single action per click)
   - Reset
   - Bet slip
   - Place Bet -> PLACE_BET payload
