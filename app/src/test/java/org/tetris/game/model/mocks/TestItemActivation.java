package org.tetris.game.model.mocks;

import java.util.ArrayList;
import java.util.List;

import org.tetris.game.model.items.ItemActivation;

import org.util.Point;

public class TestItemActivation implements ItemActivation {
        public List<Integer> clearingRows = new ArrayList<>();
        public List<Integer> clearingCols = new ArrayList<>();
        public List<Point> clearingCells = new ArrayList<>();

        @Override
        public void addClearingRow(int row) {
            clearingRows.add(row);
        }

        @Override
        public void addClearingCol(int col) {
            clearingCols.add(col);
        }

        @Override
        public void addClearingCells(List<Point> cells) {
            clearingCells.addAll(cells);
        }

        public void clear() {
            clearingRows.clear();
            clearingCols.clear();
            clearingCells.clear();
        }
    };
