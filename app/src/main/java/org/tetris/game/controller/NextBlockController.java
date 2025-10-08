package org.tetris.game.controller;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import org.tetris.game.model.blocks.*;

class NextBlockController {
	private ArrayList<Block> blockPool;
	private Queue<Block> nextBlocks;
	private int fillCount;
	private Random random;

	/**
	 * NextBlockController 생성자
	 * 블록 풀을 초기화하고 랜덤 생성기를 설정한 후,
	 * 다음 블록 큐를 생성하여 초기 블럭들로 채웁니다.
	 * 
	 * @param blockList 사용 가능한 블럭들의 목록
	 * @param fillCount 블럭 큐를 채울 블럭의 수
	 */

	public NextBlockController(ArrayList<Block> blockList, int fillCount) {
		this.blockPool = blockList;
		this.fillCount = fillCount;

		this.random = new Random();
		this.nextBlocks = new LinkedList<Block>();

		fill();
	}

	private void fill() {
		for (int i = 0; i < fillCount; i++) {
			var block = getRandomBlock();
			nextBlocks.add(block);
		}
	}

	private Block getRandomBlock() {
		var block = blockPool.get(random.nextInt(blockPool.size()));
		return block;
	}

	/**
	 * 다음 블록을 가져옵니다.
	 * 큐가 비어있는 경우 자동으로 새로운 랜덤 블록들로 채운 후
	 * 큐의 첫 번째 블록을 반환합니다.
	 * 
	 * @return 다음에 사용할 Block 객체
	 */
	public Block getBlock() {
		if (nextBlocks.isEmpty())
			fill();

		var block = nextBlocks.poll();
		return block;
	}
}