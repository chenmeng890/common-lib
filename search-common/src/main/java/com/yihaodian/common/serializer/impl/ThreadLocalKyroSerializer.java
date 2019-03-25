package com.yihaodian.common.serializer.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import com.yihaodian.common.serializer.YSerializer;

/**
 * kryo是一个线程不安全的类，可以使用ThreadLocal来解决
 * 
 * @author zengfenghua
 *
 */
public class ThreadLocalKyroSerializer implements YSerializer{
	
	public static ThreadLocalKyroSerializer kyroSerializer=new ThreadLocalKyroSerializer();
	
	private ThreadLocal<Kryo> kryos = new ThreadLocal<Kryo>() {
	    protected Kryo initialValue() {
	        Kryo kryo = new Kryo();
	        kryo.setDefaultSerializer(TwoWayCompatibleSerializer.class);
	        return kryo;
	    };
	};
	
    private ThreadLocalKyroSerializer(){
		
	}

	@Override
	public <T> T fromBytes(Class<T> clz, byte[] bytes) {
		Kryo kryo=kryos.get();
		Input input=new Input(bytes);
		return kryo.readObject(input, clz);
	}

	@Override
	public byte[] toBytes(Object obj) {
		Kryo kryo=kryos.get();
		Output output=new Output(8192,Integer.MAX_VALUE);
		kryo.writeObject(output, obj);
		output.close();
		return output.toBytes();
	}

}
