package opengl4.domaci1;

import com.jogamp.common.nio.Buffers;
import com.jogamp.newt.event.*;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.FPSAnimator;
import opengl4.Utilities;
import opengl4.common.glsl.ShaderProgram;
import opengl4.common.glsl.shaders.FragmentShader;
import opengl4.common.glsl.shaders.VertexShader;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Main implements GLEventListener {
	private static class FPSAnimatorStopper extends Thread {
		private final FPSAnimator fpsAnimator;
		
		public FPSAnimatorStopper ( FPSAnimator fpsAnimator ) {
			this.fpsAnimator = fpsAnimator;
		}
		
		@Override
		public void run ( ) {
			if ( this.fpsAnimator != null )	{
				this.fpsAnimator.stop ( );
			}
		}
	}

	private static final int FRAMES_PER_SECOND = 60;
	private static final int WINDOW_WIDTH      = 1200;
	private static final int WINDOW_HEIGHT     = 800;
    private static final float SCALE_X         = 4.25f;
    private static final float SCALE_Y         = 2.8f;
	private static final String TITLE          = "Mandelbrot Set";
	
	private int vertexArrayObjectId;
	private int vertexBufferObjectId;   // Stores all vertex data.
    private int elementBufferObjectId;  // Stores all index data (triplets of vertex ids that make up each triangle)
	private ShaderProgram shaderProgram;

    float previousMouseX;
    float previousMouseY;
    float offsetX = 0.0f;
    float offsetY = 0.0f;
    float zoom = 1.0f;
    float zoomInc = 0.1f;

    // USER INPUT VARIABLES:
    int MAX_N = 45;
    int N_INC = 5;
	
	public Main( ) {
		System.setProperty("jogl.disable.openglcore", "false");

		GLProfile glProfile = GLProfile.getDefault ( );
		
		System.out.println ( glProfile.getGLImplBaseClassName ( ) );
		System.out.println ( glProfile.getImplName ( ) );
		System.out.println ( glProfile.getName ( ) );
		System.out.println ( glProfile.hasGLSL ( ) );
		
		GLCapabilities glCapabilities = new GLCapabilities ( glProfile );
		glCapabilities.setAlphaBits ( 8 );
		glCapabilities.setDepthBits ( 24 );
		
		System.out.println ( glCapabilities );
		
		GLWindow window = GLWindow.create ( glCapabilities );
		
		FPSAnimator fpsAnimator = new FPSAnimator ( window, Main.FRAMES_PER_SECOND, true );
		
		window.addGLEventListener ( this );
		window.addWindowListener ( new WindowAdapter ( ) {
			@Override public void windowDestroyed ( WindowEvent e ) {
				new FPSAnimatorStopper ( fpsAnimator ).start ( );
			}
		} );

        // User input listeners:
        window.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                char keyChar = e.getKeyChar();

                if (keyChar == '+') {
                    System.out.print("Plus key pressed. ");

                    MAX_N += N_INC;

                    System.out.println("Number of iterations: " + MAX_N);
                }
                else if (keyChar == '-') {
                    System.out.print("Minus key pressed. ");

                    if (MAX_N - N_INC >= 1) {
                        MAX_N -= N_INC;
                    }

                    System.out.println("Number of iterations: " + MAX_N);
                }
            }
        });
        window.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed ( MouseEvent event ) {
                previousMouseX = event.getX ( );
                previousMouseY = event.getY ( );
            }

            @Override
            public void mouseDragged ( MouseEvent event ) {
                float currentX = event.getX();
                float currentY = event.getY();

                // Calculate delta in window pixels.
                float dx = currentX - previousMouseX;
                float dy = currentY - previousMouseY;

                // Normalize delta and scale it to match the current zoom level.
                float normDX = (dx / WINDOW_WIDTH) * zoom;
                float normDY = (dy / WINDOW_HEIGHT) * zoom;

                // Update offset so the world moves opposite of the dragging direction.
                offsetX -= normDX; // Subtraction because dragging mouse to the right should translate the world to the left.
                offsetY += normDY; // Inverse operation, because event.getY uses top-left corner as (0,0), and openGL uses bottom-left corner as (0,0).

                // Update previous mouse position for the next event.
                previousMouseX = currentX;
                previousMouseY = currentY;

                System.out.println("New offsetX and offsetY = " + offsetX + ", " + offsetY);
            }

            @Override
            public void mouseWheelMoved ( MouseEvent event ) {
                // Y-axis logic is inverted when using event.getY compared to what openGL uses.
                // Top-left corner of the screen is (0,0) and bottom-right is (WIDTH,HEIGHT), instead of bottom-left corner being (0,0).
                float mouseX = event.getX();
                float mouseY = WINDOW_HEIGHT - event.getY();  // Invert y coordinate to make it match the openGL coordinate system.

                // Calculate new zoom.
                float dz = event.getRotation ( )[1];  // Returns either 1.0 or -1.0.
                float newZoom = zoom * (dz > 0 ? (1 - zoomInc) : (1 + zoomInc));

                // New offset is the difference between the cursor's normalized world coordinates before and after zoom change.
                float worldX = (mouseX / WINDOW_WIDTH - 0.5f) * zoom + offsetX;
                float worldY = (mouseY / WINDOW_HEIGHT - 0.5f) * zoom + offsetY;
                offsetX = worldX - (mouseX / WINDOW_WIDTH - 0.5f) * newZoom;
                offsetY = worldY - (mouseY / WINDOW_HEIGHT - 0.5f) * newZoom;

                // Update zoom;
                zoom = newZoom;
            }
        });

		window.setSize ( Main.WINDOW_WIDTH, Main.WINDOW_HEIGHT );
		window.setTitle ( Main.TITLE );
		window.setVisible ( true );

		fpsAnimator.start ( );
	}

    // Initializes OpenGL resources when the drawable is created. It's called once during setup.
	@Override
	public void init ( GLAutoDrawable drawable ) {
        // Retrieves the GL4 context, which provides access to OpenGL 4.x functions for rendering.
		GL4 gl = drawable.getGL ( ).getGL4bc ( );

        // Sets the clear color (background color) to black with zero alpha. This color is used when clearing the framebuffer.
		gl.glClearColor ( 0.0f, 0.0f, 0.0f, 0.0f );

        // Creates a buffer to hold one integer, used for storing IDs for OpenGL objects like VAOs and VBOs.
		IntBuffer intBuffer = IntBuffer.allocate ( 1 );
        // Generates one Vertex Array Object (VAO) and stores its ID in intBuffer.
		gl.glGenVertexArrays ( 1, intBuffer );
        // Retrieves the generated VAO ID from the buffer and stores it in the class variable for later use.
		this.vertexArrayObjectId = intBuffer.get ( 0 );

        // Binds the VAO so that subsequent vertex attribute and buffer configurations are stored within it.
		gl.glBindVertexArray ( this.vertexArrayObjectId );

        // Initializes buffer data:
		float vertices[] = {
            -1.0f, -1.0f, 0.0f,  // 0: bottom left corner
             1.0f, -1.0f, 0.0f,  // 1: bottom right corner
             1.0f,  1.0f, 0.0f,  // 2: top right corner
            -1.0f,  1.0f, 0.0f,  // 3: top left corner
		};

        int indices[] = {
            0, 1, 2,  // bottom triangle
            0, 2, 3,  // top triangle
        };
		
		FloatBuffer verticesBuffer = Buffers.newDirectFloatBuffer ( vertices, 0 );
        IntBuffer   indicesBuffer  = Buffers.newDirectIntBuffer ( indices, 0 );

        // Resets the buffer's position to zero, preparing it for reuse.
		intBuffer.rewind ( );
        // Generates one Buffer Object (VBO), and stores its ID in intBuffer.
		gl.glGenBuffers ( 1, intBuffer );
        // Retrieves the generated VBO ID from the buffer and stores it in the class variable for later use.
		this.vertexBufferObjectId = intBuffer.get ( 0 );

        // Binds the VBO as the current array buffer, so subsequent buffer operations affect it.
		gl.glBindBuffer ( GL4.GL_ARRAY_BUFFER, this.vertexBufferObjectId );
        // Define the VBO structure (size, data and usage pattern).
		gl.glBufferData ( GL4.GL_ARRAY_BUFFER, vertices.length * Float.BYTES, verticesBuffer, GL4.GL_STATIC_DRAW );
        // Define the vertex structure in VBO by individually defining every attribute the vertices are made of:
        //   the attribute index (typically, 0 is used for position, 1 for normals, 2 for texture coordinates...),
        //   data count, data type, is_data_normalized,
        //   stride (how many bytes to skip to get from this attribute to the same attribute of the next vertex in VBO.
        //           If each vertex has 3 floats for position, and no extra data, stride can be 0 or 3*Float.BYTES),
        //   offset (amount of data to skip from the start of this vertex whole data to the start of this attribute data).
		gl.glVertexAttribPointer ( 0, 3, GL4.GL_FLOAT, false, 0, 0 );
        // Enables vertex attribute with id 0.
		gl.glEnableVertexAttribArray ( 0 );

        // Same process for the buffer of indices (EBO).
        intBuffer.rewind ( );
        gl.glGenBuffers ( 1, intBuffer );
        this.elementBufferObjectId = intBuffer.get ( 0 );
        gl.glBindBuffer ( GL4.GL_ELEMENT_ARRAY_BUFFER, this.elementBufferObjectId );
        gl.glBufferData ( GL4.GL_ELEMENT_ARRAY_BUFFER, indices.length * Integer.BYTES, indicesBuffer, GL4.GL_STATIC_DRAW );

        // Read shader files.
		String vertexShaderSource   = Utilities.readFile ( Main.class, "vertexShader.glsl" );
		String fragmentShaderSource = Utilities.readFile ( Main.class, "fragmentShader.glsl" );

        // Creates a shader program by compiling and linking the vertex and fragment shaders.
		this.shaderProgram = new ShaderProgram (
			"MandelbrotShaderProgram",
			new VertexShader ( "MandelbrotShaderProgram.vertexShader", vertexShaderSource ),
			new FragmentShader ( "MandelbrotShaderProgram.fragmentShader", fragmentShaderSource )
		);
		this.shaderProgram.initialize ( gl );

        // Binds the shader program for rendering (necessary to do before passing uniform values).
        this.shaderProgram.activate ( gl );

        // Passes uniform values.
        this.shaderProgram.setiUniform(gl, "WINDOW_WIDTH", WINDOW_WIDTH);
        this.shaderProgram.setiUniform(gl, "WINDOW_HEIGHT", WINDOW_HEIGHT);
        this.shaderProgram.setfUniform(gl, "SCALE_X", SCALE_X);
        this.shaderProgram.setfUniform(gl, "SCALE_Y", SCALE_Y);
        this.shaderProgram.setiUniform(gl, "MAX_N", MAX_N);
        this.shaderProgram.setfUniform(gl, "offsetX", offsetX);
        this.shaderProgram.setfUniform(gl, "offsetY", offsetY);
        this.shaderProgram.setfUniform(gl, "zoom", zoom);
    }

    // Cleans up resources when the drawable is destroyed.
	@Override public void dispose ( GLAutoDrawable drawable ) {
        // Gets the OpenGL context for cleanup operations.
		GL4 gl = drawable.getGL ( ).getGL4bc ( );

        // Deletes the shader program to free GPU resources.
		this.shaderProgram.destroy ( gl );

        // Creates a buffer to hold IDs for buffer deletion.
		IntBuffer intBuffer = IntBuffer.allocate ( 2 );
        // Store IDs of VBO adn EBO.
		intBuffer.put ( this.vertexBufferObjectId );
        intBuffer.put ( this.elementBufferObjectId );
        // Resets buffer position for reading.
		intBuffer.rewind ( );
        // Deletes these buffers from GPU memory.
		gl.glDeleteBuffers ( 2, intBuffer );

        // Resets buffer starting position.
		intBuffer.rewind ( );
        // Puts the VAO ID into the buffer.
		intBuffer.put ( this.vertexArrayObjectId );
        // Resets buffer position for reading.
		intBuffer.rewind ( );
        // Deletes the VAO from GPU memory.
		gl.glDeleteVertexArrays ( 1, intBuffer );
		
	}

    // Called every frame to render the scene.
	@Override
	public void display ( GLAutoDrawable drawable ) {
        // Retrieves the OpenGL context for rendering.
        GL4 gl = drawable.getGL().getGL4bc();

        // Binds the shader program for rendering.
        this.shaderProgram.activate(gl);

        // Passes uniform values:
        this.shaderProgram.setiUniform(gl, "MAX_N", MAX_N);
        this.shaderProgram.setfUniform(gl, "offsetX", offsetX);
        this.shaderProgram.setfUniform(gl, "offsetY", offsetY);
        this.shaderProgram.setfUniform(gl, "zoom", zoom);

        // Clears the color, stencil, and depth buffers to prepare for new frame rendering.
        gl.glClear(GL4.GL_COLOR_BUFFER_BIT | GL4.GL_STENCIL_BUFFER_BIT | GL4.GL_DEPTH_BUFFER_BIT);

        // Draws two triangles (rectangle) using the currently bound vertex data, starting at index 0, with 6 vertices.
        gl.glDrawElements(GL4.GL_TRIANGLES, 6, GL4.GL_UNSIGNED_INT, 0);
	}
	
	@Override public void reshape ( GLAutoDrawable drawable, int x, int y, int width, int height ) { }
	
	public static void main ( String[] args ) {
		new Main ( );
	}
}
