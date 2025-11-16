package org.tetris.game.model.items;

import java.util.List;
import org.util.Point;

/**
 * Item 활성화 시 삭제할 행/열/셀을 등록하는 인터페이스
 * GameController나 DualGameController 등 다양한 컨트롤러가 구현 가능
 */
public interface ItemActivation {
    /**
     * 삭제할 행 추가
     * @param row 삭제할 행 번호
     */
    void addClearingRow(int row);
    
    /**
     * 삭제할 열 추가
     * @param col 삭제할 열 번호
     */
    void addClearingCol(int col);
    
    /**
     * 삭제할 셀들 추가
     * @param cells 삭제할 셀 좌표 리스트
     */
    void addClearingCells(List<Point> cells);
}
