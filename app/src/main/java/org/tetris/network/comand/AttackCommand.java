package org.tetris.network.comand;

import java.util.List;

public class AttackCommand implements GameCommand {
    private final List<int[]> attackRows;

    public AttackCommand(List<int[]> attackRows) {
        this.attackRows = attackRows;
    }

    public List<int[]> getAttackRows() {
        return attackRows;
    }

    @Override
    public void execute(GameCommandExecutor executor) {
        executor.attack(attackRows);
    }
}
