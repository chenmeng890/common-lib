package com.yihaodian.common.serializer;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.minlog.Log;
import com.yihaodian.common.serializer.impl.TwoWayCompatibleSerializer;

public class KryoRW {

	public static void testWriteFile(Kryo kryo) {
		try {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			Output output = new Output(outStream, 4096);
			kryo.writeObject(output, new TestObject(1L));
			output.flush();
			
			FileOutputStream file = new FileOutputStream("D:\\kryo.out");
			file.write(outStream.toByteArray());
			file.flush();
			file.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void testReadFile(Kryo kryo) {
		try {
			FileInputStream in = new FileInputStream("D:\\kryo.out");
			//Object to = kryo.readObject(new Input(in), TestObject.class);
			Object to = kryo.readObject(new Input(in), TestObject.class);
			in.close();
			System.out.println(to);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		Kryo kryo = new Kryo();	
		Log.TRACE = true;
		kryo.setReferences(true);
		kryo.setDefaultSerializer(TwoWayCompatibleSerializer.class);
		//testWriteFile(kryo);
		testReadFile(kryo);
	}
}
