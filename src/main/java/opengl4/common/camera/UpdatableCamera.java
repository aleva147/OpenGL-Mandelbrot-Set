package opengl4.common.camera;

import org.joml.Matrix4f;

public class UpdatableCamera extends Camera {
	private static Matrix4f getProjectionMatrix ( float aspect, float zNear, float zFar ) {
		return new Matrix4f ( ).perspective ( ( float ) Math.toRadians ( 45.0 ), aspect, zNear, zFar );
	}
	
	private float z;
	private float xAngle;
	private float yAngle;
	
	public UpdatableCamera ( float z,float xAngle, float yAngle, float aspect, float zNear, float zFar ) {
		super (
			new Matrix4f ( ),
			UpdatableCamera.getProjectionMatrix ( aspect, zNear, zFar )
		);
		
		this.z      = z;
		this.xAngle = xAngle;
		this.yAngle = yAngle;
		
		this.updateView ( );
	}
	
	private void updateView ( ) {
		super.getView ( ).identity ( )
		     .translate ( 0, 0, this.z )
		     .rotate ( ( float ) Math.toRadians ( this.xAngle ), 1, 0, 0 )
		     .rotate ( ( float ) Math.toRadians ( this.yAngle ), 0, 1, 0 );
	}
	
	public void move ( float zStep ) {
		this.z += zStep;
		
		this.updateView ( );
	}
	
	public void yRotate ( float yAngleStep ) {
		this.yAngle += yAngleStep;
		
		this.updateView ( );
	}
	
	public void xRotate ( float xAngleStep ) {
		this.xAngle += xAngleStep;
		
		this.updateView ( );
	}
}
