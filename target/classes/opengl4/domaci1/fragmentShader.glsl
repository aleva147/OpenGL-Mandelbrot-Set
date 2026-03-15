#version 430

uniform int WINDOW_WIDTH;
uniform int WINDOW_HEIGHT;
uniform float SCALE_X;
uniform float SCALE_Y;
uniform int MAX_N;
uniform float offsetX;
uniform float offsetY;
uniform float zoom;

out vec4 outColor;


// Function to convert HSV to RGB:
vec3 hsvToRgb(float hue, float saturation, float value) {
    // Chroma represents the colorfulness intensity, i.e. saturation (c == 0 is gray, c == v is full brightness).
    float chroma = value * saturation;
    // Hue is a degree on the color wheel [0,360]. Dividing by 60 tells us in which of the 6 color sectors our color is.
    float colorSector = hue / 60;
    // X is an interpolated value between the two colors that are the extremums of the color sector.
    float x = chroma * (1.0 - abs(mod(colorSector, 2.0) - 1.0));

    vec3 rgb;

    // Red -> Yellow sector:
    if (0 <= colorSector && colorSector < 1) {
        rgb = vec3(chroma, x, 0.0);
    }
    // Yellow -> Green sector:
    else if (1 <= colorSector && colorSector < 2) {
        rgb = vec3(x, chroma, 0.0);
    }
    // Green -> Cyan sector:
    else if (2 <= colorSector && colorSector < 3) {
        rgb = vec3(0.0, chroma, x);
    }
    // Cyan -> Blue sector:
    else if (3 <= colorSector && colorSector < 4) {
        rgb = vec3(0.0, x, chroma);
    }
    // Blue -> Magenta sector:
    else if (4 <= colorSector && colorSector < 5) {
        rgb = vec3(x, 0.0, chroma);
    }
    // Magenta -> Red sector:
    else if (5 <= colorSector && colorSector < 6) {
        rgb = vec3(chroma, 0.0, x);
    }
    // This case should never happen:
    else {
        rgb = vec3(0.0, 0.0, 0.0);
    }

    // Corrects the brightness of the resulting rgb color.
    float brightness = value - chroma;
    return rgb + vec3(brightness);
}

void main ( ) {
    // gl_FragCoord returns non-normalized window coordinates (bottom-left corner of the screen is (0,0), top right corner is (WINDOW_WIDTH,WINDOW_HEIGHT)).
    // Normalize coordinates by dividing them with screen width or height (bottom-left corner is (0,0), top right corner is (1,1)).
    float normX = gl_FragCoord.x / WINDOW_WIDTH;
    float normY = gl_FragCoord.y / WINDOW_HEIGHT;

    // By subtracting 0.5f, translate the coordinate origin (0,0) to the middle of the screen.
    // Scale and translate the world based on user input.
    // Last multiplication is just to make the size of the whole world a bit smaller on startup, to make it fit nicely into screen.
    float cReal = ((normX - 0.5) * zoom + offsetX) * SCALE_X;
    float cImag = ((normY - 0.5) * zoom + offsetY) * SCALE_Y;

    int n = 0;
    float zReal = 0.0f;
    float zImag = 0.0f;

    while (n < MAX_N) {
        // z_n+1 = (zReal + i * zImag)^2 + cReal + i * cImag
        //       = zReal^2 - zImag^2 + cReal - i * (2 * zReal * zImag + cImag)

        float newZReal = zReal * zReal - zImag * zImag + cReal;
        float newZImag = 2 * zReal * zImag + cImag;

        float radius = sqrt(newZReal * newZReal + newZImag * newZImag);  // How far the resulting value is from the coordinate origin (middle of the screen).

        // The larger the deciding value, the more details there are in the drawing.
        if (radius > 2) break;  // Alternatively, don't use sqrt above and use 4 instead of 2 here to save on processing time.

        zReal = newZReal;
        zImag = newZImag;
        n++;
    }

    // Use black color if this pixel coordinates give a convergent value when used as c in: z_n+1 = z_n^2 + c.
    if (n == MAX_N) {
        outColor = vec4(0.0f, 0.0f, 0.0f, 1.0f);
    }
    // Otherwise, create an HSV color based on the number of iterations, and then convert it to a RGB value.
    else {
        float hScalingFactor = 360 / MAX_N;  // The defined range for H values in HSV is [0,360] (for S and V the range is [0.0,1.0]).
        vec3 rgbColor = hsvToRgb(n * hScalingFactor, 1.0f, 1.0f);
        outColor = vec4(rgbColor, 1.0);
    }
}
