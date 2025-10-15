package org.tetris.shared;

import org.tetris.Router;

/**
 * Router를 주입받을 수 있는 컨트롤러 인터페이스
 */
public interface RouterAware {
    void setRouter(Router router);
}