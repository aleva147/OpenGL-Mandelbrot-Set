package opengl4.common.scene;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import opengl4.common.camera.Camera;
import opengl4.common.graphicsObject.AbstractGraphicsObject;
import org.joml.Matrix4f;

public class Scene {
	protected Camera                 camera;
	protected AbstractGraphicsObject abstractGraphicsObjects[];
    
    public Scene ( Camera camera, AbstractGraphicsObject ... abstractGraphicsObjects ) {
        this.camera = camera;
        
        this.abstractGraphicsObjects = new AbstractGraphicsObject[abstractGraphicsObjects.length];
        for ( int i = 0; i < this.abstractGraphicsObjects.length; ++i ) {
            this.abstractGraphicsObjects[i] = abstractGraphicsObjects[i];
        }
    }
	
	public void initialize ( GLAutoDrawable drawable ) {
		GL4 gl = drawable.getGL ( ).getGL4bc ( );
		for ( AbstractGraphicsObject graphicsObject : this.abstractGraphicsObjects ) {
			graphicsObject.initialize ( gl );
		}
	}
	
	public void update ( ) {
		this.camera.update ( );
		for ( AbstractGraphicsObject graphicsObject : this.abstractGraphicsObjects ) {
			graphicsObject.update ( );
		}
	}
	
	public void display ( GLAutoDrawable drawable ) {
		GL4 gl = drawable.getGL ( ).getGL4bc ( );
		
		Matrix4f identity = new Matrix4f ( );
		for ( AbstractGraphicsObject graphicsObject : this.abstractGraphicsObjects ) {
			graphicsObject.render ( gl, identity, this.camera );
		}
	}
	
	public void destroy ( GL4 gl4 ) {
		for ( AbstractGraphicsObject graphicsObject : this.abstractGraphicsObjects ) {
			graphicsObject.destroy ( gl4 );
		}
	}
}
