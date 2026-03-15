package opengl4;

import de.matthiasmann.twl.utils.PNGDecoder;
import org.joml.Vector3f;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class Utilities {
	public static String readFile  ( Class scope, String filename ) {
		String returnString = null;
		
		try ( InputStream inputStream = scope.getResourceAsStream ( filename ) ) {
			returnString = new String ( inputStream.readAllBytes ( ) );
		} catch ( IOException | NullPointerException exception ) {
			exception.printStackTrace ( );
		}
		
		return returnString;
	}
	
	public static class PNG {
		private int width;
		private int height;
		private ByteBuffer data;
		
		public PNG ( int width, int height, ByteBuffer data ) {
			this.width = width;
			this.height = height;
			this.data = data;
		}
		
		public int getWidth ( ) {
			return width;
		}
		
		public int getHeight ( ) {
			return height;
		}
		
		public ByteBuffer getData ( ) {
			return data;
		}
	}
	
	public static PNG loadPNG ( Class scope, String filename ) {
		PNG result = null;
		
		try (
				InputStream in = scope.getResourceAsStream ( filename )
		) {
			PNGDecoder decoder = new PNGDecoder ( in );
			
			int        width  = decoder.getWidth ( );
			int        height = decoder.getHeight ( );
			ByteBuffer data   = ByteBuffer.allocate ( 4 * width * height );
			
			decoder.decode ( data, width * 4, PNGDecoder.Format.RGBA );
			
			data.flip ( );
			
			result = new PNG ( width, height, data );
		} catch ( IOException e ) {
			e.printStackTrace ( );
		}
		
		return result;
	}
	
	private static Vector3f makeVector ( float[] vertices, int index0, int index1 ) {
		float x = vertices[index1 + 0] - vertices[index0 + 0];
		float y = vertices[index1 + 1] - vertices[index0 + 1];
		float z = vertices[index1 + 2] - vertices[index0 + 2];
		
		return new Vector3f ( x, y, z );
	}
	
	public static float[] computeNormals ( float[] vertices ) {
		float[] normals = new float[vertices.length];
		
		for ( int i = 0; i < ( vertices.length / 9 ); ++i ) {
			int index0 = i * 9;
			int index1 = i * 9 + 3;
			int index2 = i * 9 + 6;
			
			Vector3f vector0 = Utilities.makeVector ( vertices, index0, index1 );
			Vector3f vector1 = Utilities.makeVector ( vertices, index0, index2 );
			
			Vector3f normal = vector0.cross ( vector1 );
			
			for ( int j = 0; j < 3; ++j ) {
				normals[i * 9 + j * 3 + 0] = normal.x;
				normals[i * 9 + j * 3 + 1] = normal.y;
				normals[i * 9 + j * 3 + 2] = normal.z;
			}
		}
		
		return normals;
	}
}
