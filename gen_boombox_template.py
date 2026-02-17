from PIL import Image, ImageDraw, ImageFont

W, H = 360, 260
img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
draw = ImageDraw.Draw(img)

try:
    font = ImageFont.truetype("arial.ttf", 9)
    font_sm = ImageFont.truetype("arial.ttf", 8)
except:
    font = ImageFont.load_default()
    font_sm = font

# Widget layout constants (must match Java code)
WX = 80
WY = 55
WWIDTH = 200

# Cassette deck bounds
DECK_X1 = WX - 6
DECK_X2 = WX + WWIDTH + 6
DECK_Y1 = 40
DECK_Y2 = 190

# Display panel bounds
DISP_X1 = WX - 10
DISP_X2 = WX + WWIDTH + 10
DISP_Y1 = 200
DISP_Y2 = 248

# Body outline (1px)
draw.rectangle([3, 28, W-4, H-4], outline=(255, 255, 255, 255))

# Badge area for title text
badge_x1 = W // 2 - 35
badge_x2 = W // 2 + 35
draw.rectangle([badge_x1, DECK_Y1 + 1, badge_x2, DECK_Y1 + 10], outline=(200, 200, 100, 255))

def label(x, y, w, h, text, col=(255, 255, 0, 200)):
    draw.rectangle([x, y, x + w - 1, y + h - 1], outline=col)
    try:
        tw = draw.textlength(text, font=font_sm)
    except:
        tw = len(text) * 5
    tx = x + (w - tw) // 2
    ty = y + (h - 9) // 2
    draw.text((tx, ty), text, fill=col, font=font_sm)

# URL text field
label(WX, WY, 200, 20, "URL TEXT FIELD")

# Play / Pause / Stop buttons
label(WX, WY + 30, 63, 20, "PLAY")
label(WX + 68, WY + 30, 63, 20, "PAUSE")
label(WX + 136, WY + 30, 64, 20, "STOP")

# Volume slider
label(WX, WY + 58, 200, 20, "VOLUME SLIDER")

# Range slider
label(WX, WY + 82, 200, 20, "RANGE SLIDER")

# Transport controls row
label(WX, WY + 110, 80, 20, "LOOP")
label(WX + 84, WY + 110, 25, 20, "<<")
label(WX + 112, WY + 110, 30, 20, "-15s")
label(WX + 145, WY + 110, 30, 20, "+15s")
label(WX + 178, WY + 110, 22, 20, ">>")

# Display panel sections
green = (100, 255, 100, 200)
red = (255, 100, 100, 200)
label(DISP_X1 + 2, DISP_Y1 + 3, DISP_X2 - DISP_X1 - 4, 14, "TRACK INFO", green)
label(DISP_X1 + 2, DISP_Y1 + 20, DISP_X2 - DISP_X1 - 4, 12, "STATUS", green)
label(DISP_X1 + 2, DISP_Y1 + 35, DISP_X2 - DISP_X1 - 4, 11, "ERROR", red)

# Title label
label(badge_x1 + 2, DECK_Y1 + 2, badge_x2 - badge_x1 - 4, 8, "TITLE", (255, 200, 100, 200))

out = r"e:\Modding\Soundscape\boombox_template.png"
img.save(out)
print(f"Template saved: {W}x{H} -> {out}")
