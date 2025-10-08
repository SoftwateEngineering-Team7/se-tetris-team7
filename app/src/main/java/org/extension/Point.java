package org.extension;

public class Point {
    public int r;
    public int c;

    /**
     * Point 생성자
     * @param r 행 좌표
     * @param c 열 좌표
     */
    public Point(int r, int c) {
        this.r = r;
        this.c = c;
    }

    /**
     * 현재 좌표에 주어진 값을 더합니다.
     * @param r 더할 행 좌표값
     * @param c 더할 열 좌표값
     * @return 더한 결과의 새로운 Point 객체
     */
    public Point add(int r, int c)
    {
        return new Point(this.r + r, this.c + c);
    }

    /**
     * 현재 좌표에 다른 Point의 좌표를 더합니다.
     * @param pos 더할 Point 객체
     * @return 두 Point를 더한 결과의 새로운 Point 객체
     */
    public Point add(Point pos)
    {
        return new Point(r + pos.r, c + pos.c);
    }

    /**
     * 현재 좌표에서 주어진 값을 뺍니다.
     * @param r 뺄 행 좌표값
     * @param c 뺄 열 좌표값
     * @return 뺀 결과의 새로운 Point 객체
     */
    public Point subtract(int r, int c)
    {
        return new Point(this.r - r, this.c - c);
    }

    /**
     * 현재 좌표에서 다른 Point의 좌표를 뺍니다.
     * @param pos 뺄 Point 객체
     * @return 두 Point를 뺀 결과의 새로운 Point 객체
     */
    public Point subtract(Point pos)
    {
        return new Point(r - pos.r, c - pos.c);
    }

    @Override
    public boolean equals(Object obj)
    {
        if(!(obj instanceof Point)) return false;
        Point pos = (Point)obj;
        return r == pos.r && c == pos.c;
    }

    @Override
    public int hashCode()
    {
        return r * 31 + c; // 20*10이니 (r < 31) 충돌 없음
        // return Objects.hash(r, c); // r > 31이면 고려
    }

    @Override
    public String toString() 
    {
        StringBuilder str = new StringBuilder();
        str.append("(").append(r).append(", ").append(c).append(")");
        return str.toString();
    }

}
