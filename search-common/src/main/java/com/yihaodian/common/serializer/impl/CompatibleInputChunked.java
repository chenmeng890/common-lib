package com.yihaodian.common.serializer.impl;
import static com.esotericsoftware.minlog.Log.TRACE;
import static com.esotericsoftware.minlog.Log.trace;

import java.io.IOException;
import java.io.InputStream;

import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;

/**
 * 代码来自于InputChunked，并为兼容性设计增加的新功能
 * @author zhouhang
 *
 */
public class CompatibleInputChunked extends Input {
	private int chunkSize = -1;

	/** Creates an uninitialized InputChunked with a buffer size of 2048. The InputStream must be set before it can be used. */
	public CompatibleInputChunked () {
		super(2048);
	}

	/** Creates an uninitialized InputChunked. The InputStream must be set before it can be used. */
	public CompatibleInputChunked (int bufferSize) {
		super(bufferSize);
	}

	/** Creates an InputChunked with a buffer size of 2048. */
	public CompatibleInputChunked (InputStream inputStream) {
		super(inputStream, 2048);
	}

	public CompatibleInputChunked (InputStream inputStream, int bufferSize) {
		super(inputStream, bufferSize);
	}

	public void setInputStream (InputStream inputStream) {
		super.setInputStream(inputStream);
		chunkSize = -1;
	}

	public void setBuffer (byte[] bytes, int offset, int count) {
		super.setBuffer(bytes, offset, count);
		chunkSize = -1;
	}

	public void rewind () {
		super.rewind();
		chunkSize = -1;
	}

	protected int fill (byte[] buffer, int offset, int count) throws KryoException {
		if (chunkSize == -1) // No current chunk, expect a new chunk.
			readChunkSize();
		else if (chunkSize == 0) // End of chunks.
			return -1;
		int actual = super.fill(buffer, offset, Math.min(chunkSize, count));
		chunkSize -= actual;
		if (chunkSize == 0) readChunkSize(); // Read next chunk size.
		return actual;
	}

	private void readChunkSize () {
		try {
			InputStream inputStream = getInputStream();
			for (int offset = 0, result = 0; offset < 32; offset += 7) {
				int b = inputStream.read();
				if (b == -1) throw new KryoException("Buffer underflow.");
				result |= (b & 0x7F) << offset;
				if ((b & 0x80) == 0) {
					chunkSize = result;
					if (TRACE) {
						trace("kryo", "Read chunk: " + chunkSize);						
					}
					return;
				}
			}
		} catch (IOException ex) {
			throw new KryoException(ex);
		}
		throw new KryoException("Malformed integer.");
	}
	
	/**
	 * 读取下一个Chunk的大小
	 * NOTE：这个函数会操作InputStream，所以是不可重入的。	
	 * @return
	 */
	public int nextChunkSize () {
		try {
			InputStream inputStream = getInputStream();
			for (int offset = 0, result = 0; offset < 32; offset += 7) {
				int b = inputStream.read();
				if (b == -1) throw new KryoException("Buffer underflow.");
				result |= (b & 0x7F) << offset;
				if ((b & 0x80) == 0) {
					chunkSize = result;
					if (TRACE) {
						trace("kryo", "nextChunkSize Read chunk: " + chunkSize);						
					}
					return result;
				}
			}
		} catch (IOException ex) {
			throw new KryoException(ex);
		}
		throw new KryoException("Malformed integer.");
	}

	/** Advances the stream to the next set of chunks. InputChunked will appear to hit the end of the data until this method is
	 * called. */
	public void nextChunks () {
		if (TRACE) trace("kryo", "nextChunks, Chunk size: " + chunkSize + ", position: " + position() + ",  limit: " + limit());
		if (chunkSize == -1) readChunkSize(); // No current chunk, expect a new chunk.
		while (chunkSize > 0) {
			if (TRACE) trace("kryo", "nextChunks, Chunk size: " + chunkSize + ", position: " + position() + ",  limit: " + limit());
			skip(chunkSize);
		}
		chunkSize = -1;
		if (TRACE) trace("kryo", "Next chunks.");
	}
	

	/**
	 * 移动到Buffer的结尾。
	 * 适用于Buffer读到一半就放弃的场景，比如兼容性中尝试读取class类型。
	 */
	public void endBuffer () {
		if (TRACE) trace("kryo", "endBuffer, Chunk size: " + chunkSize + ", position: " + position() + ",  limit: " + limit());
		/**
		 * （1）chunkSize == 0
		 * 参考OutputChunked.endChunks(), 两个Chunk之间是使用一个0长度的空chunk做分割的
		 * 如果chunkSize == 0， 说明上一个chunk已经全部加载到了Buffer， 这个时候需要移动到Buffer的结尾。 
		 * （2）chunkSize ！= 0
		 * 参考fill()和nextChunks()，chunkSize的含义是chunk中未加载到buffer的剩余数据量，
		 * 这时我们也需要移动到buffer的结尾，让nextChunks()的语义正确。
		 */	
		if(position() != limit()) {
			setPosition(limit());
		}
	}
	
}
