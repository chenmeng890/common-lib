package com.yihaodian.common.serializer.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.DefaultSerializers.StringSerializer;
import com.esotericsoftware.kryo.serializers.DeflateSerializer;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.yihaodian.common.serializer.YSerializer;

/**
 *  please re-use KryoSerializer instead of new instance everytime.
 *  otherwise Deflater would cause Memory-leak issue.
 *  For we do not call end().
 *  Reuse Deflater to avoid the need for mass finalization.
 */
public class KryoSerializer implements YSerializer {
	private boolean deflate = true;
	private boolean noHeaders = true;
	private int compressionLevel = 6; // compromise between speed and
	private Deflater deflater = null;
	private Inflater inflater = null;
	Kryo kyro = null;

/*	use synchronized to guarantee thread-safe

	private static ThreadLocal<Kryo> _kryoHolder = new ThreadLocal<Kryo>() {

		@Override
		protected Kryo initialValue() {
			Kryo kyro = new Kryo();
			kyro.setDefaultSerializer(CompatibleFieldSerializer.class);

			return kyro;
		}
	};*/

	public KryoSerializer() {
		this(true);
	}
	
	public KryoSerializer(boolean deflate) {
		this.deflate = deflate;

		if(deflate)
		{
			deflater = new Deflater(compressionLevel, noHeaders);
			inflater = new Inflater(noHeaders);	
		}

		kyro = new Kryo();
		kyro.setDefaultSerializer(TwoWayCompatibleSerializer.class);
	}
	
	protected Kryo get() {
		// return _kryoHolder.get();
		return kyro;
	}

	public void kryoRegister(Class type, boolean deflate) {
		Kryo kyro = get();

		if (deflate) {
			kyro.register(type, new DeflateSerializer(new FieldSerializer(kyro,
					type)));
			kyro.setReferences(false);
		} else {
			kyro.register(type, new FieldSerializer(kyro, type));
		}
	}

	public synchronized byte[] toBytes(Object obj) {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		OutputStream os = bout;

		if (deflate) {
			deflater.reset();
			os = new DeflaterOutputStream(bout, deflater);
		}

		Output output = new Output(os);
		get().writeObject(output, obj);
		// output.flush();
		output.close();

		return bout.toByteArray();
	}

	public synchronized <T> T fromBytes(Class<T> clz, byte[] bytes) {
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		InputStream is = bis;

		if (deflate) {
			inflater.reset();
			is = new InflaterInputStream(bis, inflater);
		}

		Input input = new Input(is);
		T t = get().readObject(input, clz);
		input.close();

		return t;
	}

	public static void main(String[] args) {
		// kryo.setReferences(false);
		KryoSerializer kyro = new KryoSerializer();
		kyro.get().setAutoReset(true);

		kyro.get().register(String.class,
				new DeflateSerializer(new StringSerializer()));
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		String str = "abscreageategadgafffffffffffffffffaffffffffba123545fffffffffffffffffffffffffff";
		Output output = new Output(outStream, 4096);

		kyro.get().writeClassAndObject(output, str);
	}
	
}
