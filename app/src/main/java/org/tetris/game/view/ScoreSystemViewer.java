// package org.tetris.game.view;

// class Viewer
// {
// 	private Pane canvas;
// 	Pane GetCanvas() => canvas;
	
// }

// class ScoreSystemViewer extends Viewer
// {
// 	Text scoreText;

// 	ScoreSystemViewer(int score)
// 	{
// 		scoreText = new Text();
// 		canvas.GetChildren().addAll(text);
// 		Update(score);
// 	}
	
// 	void SetScoreText(int x, int y) // start?
// 	{
// 		scoreText = new Text(x, y, ""); // << 
// 	} 
// 	/*
// 	UI에서 이런 부분(<<)들은 통일시켜야 하는 특성들이 있으니까 공통 함수 있으면 편할 거 같아
// 	만약 폰트를 바꿔야 하면 일일이 바꿔야 할 수도 있고
	
// 	UIProperty class 로 Font를 불러온다 해도 보통 글자크기를 키우면 전부 적용되니까
// 	UIExtention class에 smallText, middleText, largeText 이런 식으로 만들어 두면 편할 듯
// 	*/
	
// 	public void Update(int score)
// 	{
// 		scoreText.SetText(score);
// 	}
// 	/*
// 	SetText는 말 그대로 Text만 저장되기 때문에 점수가 올라가면 Update를 해주거나,
// 	지속적으로 Update가 되어야 함.
// 	*/
// }