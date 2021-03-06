package algorithm.simhash;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

/*
 * 根据自己的测试需要，做了些改动
 */

public class SimHash2 {
	private String tokens;
	private BigInteger intSimHash;
	private String strSimHash;
	private int hashbits = 64; // simhash code的位数

	public SimHash2(String tokens) {
		this.tokens = tokens;
		this.intSimHash = this.simHash();
	}

	public SimHash2(String tokens, int hashbits) {
		this.tokens = tokens;
		this.hashbits = hashbits;
		this.intSimHash = this.simHash();
	}

	public BigInteger getIntSimHash() {
		return intSimHash;
	}

	public SimHash2(Set<String> set, int hashbits) {
		this.tokens = this.getTokensFromSet(set);
		this.hashbits = hashbits;
		this.intSimHash = this.simHash();
	}

	// 从hashset中取得所有的元素，用空格连接成一个String
	private String getTokensFromSet(Set<String> set) {
		Iterator<String> it = set.iterator();
		StringBuffer sb = new StringBuffer();
		String item = "";
		while (it.hasNext()) {
			item = (String) it.next();
			sb.append(item).append(" ");
		}
		return sb.toString();
	}

	// 获得tokens的 simhash值 整数形式 和 字符串形式
	public BigInteger simHash() {
		// 初始化一个64维的特征向量
		final int[] v = new int[this.hashbits];

		// 利用默认的分词器来对字符串进行分词
		final StringTokenizer stringTokens = new StringTokenizer(this.tokens);
		//
		// System.out.println("Token Count: " + stringTokens.countTokens());

		while (stringTokens.hasMoreTokens()) {
			final String temp = stringTokens.nextToken();
			final BigInteger t = this.hash(temp);

			// 分别处理每个分词，计算其特征hash，而后针对其某一位是否为1，对向量v进行加减1操作
			// 是否也要保证每个token的hash值的位数是hashbits？
			for (int i = 0; i < this.hashbits; i++) {
				final BigInteger bitmask = new BigInteger("1").shiftLeft(i);
				if (t.and(bitmask).signum() != 0) {
					v[i] += 1;
				} else {
					v[i] -= 1;
				}
			}
		}
		// 根据simhash的原理，特征向量中大于0的部分，对应于hash值的那一位是1
		BigInteger fingerprint = new BigInteger("0");
		final StringBuffer simHashBuffer = new StringBuffer();
		for (int i = 0; i < this.hashbits; i++) {
			if (v[i] >= 0) {
				fingerprint = fingerprint.add(new BigInteger("1").shiftLeft(i));
				simHashBuffer.append("1");
			} else {
				simHashBuffer.append("0");
			}
		}
		this.strSimHash = simHashBuffer.toString();
		System.out.println(this.strSimHash + " length "
				+ this.strSimHash.length());
		return fingerprint;
	}

	// 这是什么hash算法？？？？能否替换成 sha-1
	private BigInteger hash(String source) {
		if (source == null || source.length() == 0) {
			return new BigInteger("0");
		} else {
			char[] sourceArray = source.toCharArray();
			BigInteger x = BigInteger.valueOf(((long) sourceArray[0]) << 7);
			BigInteger m = new BigInteger("1000003"); // 大素数
			BigInteger mask = new BigInteger("2").pow(this.hashbits).subtract(
					new BigInteger("1"));
			for (char item : sourceArray) {
				BigInteger temp = BigInteger.valueOf((long) item);
				x = x.multiply(m).xor(temp).and(mask);
			}
			x = x.xor(new BigInteger(String.valueOf(source.length())));
			if (x.equals(new BigInteger("-1"))) {
				x = new BigInteger("-2");
			}
			return x;
		}
	}

	public int hammingDistance(SimHash2 other) {
		BigInteger x = this.intSimHash.xor(other.intSimHash);
		int tot = 0;

		// 统计二进制表示的x中1的个数
		// 这是个经典的算法，n&(n-1)可以每次消去最右边的1（最右边可能是一个域）
		while (x.signum() != 0) {
			tot += 1;
			x = x.and(x.subtract(new BigInteger("1")));
		}
		return tot;
	}

	public int getDistance(String str1, String str2) {
		int distance;
		if (str1.length() != str2.length()) {
			distance = -1;
		} else {
			distance = 0;
			for (int i = 0; i < str1.length(); i++) {
				if (str1.charAt(i) != str2.charAt(i)) {
					distance++;
				}
			}
		}
		return distance;
	}

	public List subByDistance(SimHash2 simHash, int distance) {
		int numEach = this.hashbits / (distance + 1);
		List characters = new ArrayList();

		StringBuffer buffer = new StringBuffer();

		int k = 0;
		for (int i = 0; i < this.intSimHash.bitLength(); i++) {
			boolean sr = simHash.intSimHash.testBit(i);

			if (sr) {
				buffer.append("1");
			} else {
				buffer.append("0");
			}

			if ((i + 1) % numEach == 0) {
				BigInteger eachValue = new BigInteger(buffer.toString(), 2);
				System.out.println("----" + eachValue);
				buffer.delete(0, buffer.length());
				characters.add(eachValue);
			}
		}

		return characters;
	}
}
