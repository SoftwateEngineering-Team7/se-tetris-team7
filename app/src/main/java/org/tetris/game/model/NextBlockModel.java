package org.tetris.game.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.function.Supplier;

import org.tetris.game.model.blocks.*;

public class NextBlockModel
{
	private int[] blockProbList;
	private int totalProb;
	private Queue<Block> nextBlocks;
	private int fillCount;
	private Random random;

	private static final ArrayList<Supplier<Block>> blockPoolList = new ArrayList<Supplier<Block>>() {{
		add(() -> new IBlock());
		add(() -> new JBlock());
		add(() -> new LBlock());
		add(() -> new OBlock());
		add(() -> new SBlock());
		add(() -> new TBlock());
		add(() -> new ZBlock());
	}};

	public final static int[] DEFAULT_BLOCK_PROB_LIST = new int[] {30, 30, 30, 30, 30, 30, 30};
	public final static int[] EASY_BLOCK_PROB_LIST = new int[] {36, 29, 29, 29, 29, 29, 29};
	public final static int[] HARD_BLOCK_PROB_LIST = new int[] {24, 31, 31, 31, 31, 31, 31};

	/**
	 * NextBlockController 생성자
	 * 블록 풀을 초기화하고 랜덤 생성기를 설정한 후, 
	 * 다음 블록 큐를 생성하여 초기 블럭들로 채웁니다.
	 * 
	 * @param blockProbList 블럭별 출현 확률 리스트 (I, J, L, O, S, T, Z 순서)
	 * @param fillCount 블럭 큐를 채울 블럭의 수
	 */
	public NextBlockModel(int[] blockProbList, int fillCount) {
		this(blockProbList, fillCount, System.currentTimeMillis());
	}

	public NextBlockModel(int[] blockProbList, int fillCount, long seed) {
		this.random = new Random(seed);
		this.nextBlocks = new LinkedList<Block>();
		this.fillCount = fillCount;

		setBlockProbList(blockProbList);
	}

	public void setBlockProbList(int[] blockProbList)
	{
		if(blockProbList.length != blockPoolList.size())
			throw new IllegalArgumentException("blockProbList의 길이는 " + blockPoolList.size() + "이어야 합니다.");

		this.blockProbList = blockProbList;
		sumProb();

		nextBlocks.clear();
		fill();
	}

	private void sumProb()
	{
		totalProb = 0;
		for(var prob : blockProbList)
			totalProb += prob;
	}


	private void fill()
	{
		for(int i = 0; i < fillCount; i++)
		{
			var block = getRandomBlock();
			nextBlocks.add(block);
		}
	}

	private Block getRandomBlock()
	{
		var rand = random.nextInt(totalProb);
		var blockSupplier = blockPoolList.get(0); // 기본값 설정
		for(int i = 0; i < blockProbList.length; i++)
		{
			rand -= blockProbList[i];
			if(rand < 0)
			{
				blockSupplier = blockPoolList.get(i);
				return blockSupplier.get();
			}
		}
		return blockSupplier.get();
	}

	/**
	 * 다음 블록을 가져옵니다.
	 * 큐가 비어있는 경우 자동으로 새로운 랜덤 블록들로 채운 후 
	 * 큐의 첫 번째 블록을 반환합니다.
	 * 
	 * @return 다음에 사용할 Block 객체
	 */
	public Block getBlock() {
		if(nextBlocks.isEmpty())
			fill();

		var block = nextBlocks.poll();
		return block;
	}

	/**
	 * 큐에서 제거하지 않고 다음 블록을 미리 확인합니다.
	 * 큐가 비어있는 경우 자동으로 새로운 랜덤 블록들로 채운 후 
	 * 큐의 첫 번째 블록을 반환합니다.
	 * 
	 * @return 다음에 사용할 Block 객체 (큐에서 제거되지 않음)
	 */
	public Block peekNext() {
		if(nextBlocks.isEmpty())
			fill();

		return nextBlocks.peek();
	}

	/**
	 * 큐의 맨 앞 블록을 새로운 블록으로 교체합니다.
	 * 큐가 비어있는 경우 자동으로 새로운 랜덤 블록들로 채운 후 교체합니다.
	 * 
	 * @param block 교체할 새로운 블록
	 */
	public void swapNext(Block block)
	{
		if(nextBlocks.isEmpty())
			fill();

		((LinkedList<Block>) nextBlocks).set(0,block);
	}
}