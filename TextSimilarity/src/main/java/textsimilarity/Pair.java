package textsimilarity;

import java.util.ArrayList;
import java.util.Set;

public class Pair {
	String name1;
	int length1;
	ArrayList<String> content1;
	Set<Integer> similars1;
	double persentage1;
	ArrayList<ArrayList<Point>> dhvtmj;
	
	String name2;
	int length2;
	ArrayList<String> content2;
	Set<Integer> similars2;
	double persentage2;
	
	public Pair(String name1, int length1, Set<Integer> similars1, double persentage1,
			String name2, int length2, Set<Integer> similars2, double persentage2) {
		super();
		this.name1 = name1;
		this.length1 = length1;
		this.similars1 = similars1;
		this.persentage1 = persentage1;
		this.name2 = name2;
		this.length2 = length2;
		this.similars2 = similars2;
		this.persentage2 = persentage2;
	}

	public String getName1() {
		return name1;
	}

	public void setName1(String name1) {
		this.name1 = name1;
	}

	public int getLength1() {
		return length1;
	}

	public void setLength1(int length1) {
		this.length1 = length1;
	}

	public ArrayList<String> getContent1() {
		return content1;
	}

	public void setContent1(ArrayList<String> content1) {
		this.content1 = content1;
	}

	public Set<Integer> getSimilars1() {
		return similars1;
	}

	public void setSimilars1(Set<Integer> similars1) {
		this.similars1 = similars1;
	}

	public double getPersentage1() {
		return persentage1;
	}

	public void setPersentage1(double persentage1) {
		this.persentage1 = persentage1;
	}

	public String getName2() {
		return name2;
	}

	public void setName2(String name2) {
		this.name2 = name2;
	}

	public int getLength2() {
		return length2;
	}

	public void setLength2(int length2) {
		this.length2 = length2;
	}

	public ArrayList<String> getContent2() {
		return content2;
	}

	public void setContent2(ArrayList<String> content2) {
		this.content2 = content2;
	}

	public Set<Integer> getSimilars2() {
		return similars2;
	}

	public void setSimilars2(Set<Integer> similars2) {
		this.similars2 = similars2;
	}

	public double getPersentage2() {
		return persentage2;
	}

	public void setPersentage2(double persentage2) {
		this.persentage2 = persentage2;
	}

	public ArrayList<ArrayList<Point>> getDhvtmj() {
		return dhvtmj;
	}

	public void setDhvtmj(ArrayList<ArrayList<Point>> dhvtmj) {
		this.dhvtmj = dhvtmj;
	}
	
}
