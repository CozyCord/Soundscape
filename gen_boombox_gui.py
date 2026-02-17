from PIL import Image, ImageDraw, ImageFont
import math

W, H = 360, 260
img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
draw = ImageDraw.Draw(img)

try:
    font = ImageFont.truetype("arial.ttf", 8)
    font_sm = ImageFont.truetype("arial.ttf", 7)
except:
    font = ImageFont.load_default()
    font_sm = font

# === COLOR PALETTE ===
BODY_DARK = (45, 42, 48)
BODY_MID = (62, 58, 66)
BODY_LIGHT = (78, 73, 82)
BODY_HIGHLIGHT = (95, 88, 100)
CASSETTE_BG = (30, 28, 35)
CASSETTE_BORDER = (80, 75, 88)
SPEAKER_DARK = (25, 23, 30)
SPEAKER_MID = (40, 37, 45)
SPEAKER_RING = (55, 50, 60)
SPEAKER_HIGHLIGHT = (70, 65, 78)
HANDLE_DARK = (50, 46, 55)
HANDLE_LIGHT = (72, 67, 78)
DISPLAY_BG = (15, 20, 12)
DISPLAY_BORDER = (60, 70, 55)
SCREW = (90, 85, 95)
SCREW_HOLE = (35, 32, 40)
ACCENT = (180, 60, 50)
ACCENT2 = (50, 160, 180)
RECESS = (22, 20, 28)
RECESS_BORDER = (50, 46, 55)

def draw_rounded_rect(d, x1, y1, x2, y2, r, fill, outline=None):
    max_r = min((x2 - x1) // 2, (y2 - y1) // 2)
    r = min(r, max(0, max_r))
    if r <= 0:
        d.rectangle([x1, y1, x2, y2], fill=fill, outline=outline)
        return
    d.rectangle([x1+r, y1, x2-r, y2], fill=fill)
    d.rectangle([x1, y1+r, x2, y2-r], fill=fill)
    d.pieslice([x1, y1, x1+2*r, y1+2*r], 180, 270, fill=fill)
    d.pieslice([x2-2*r, y1, x2, y1+2*r], 270, 360, fill=fill)
    d.pieslice([x1, y2-2*r, x1+2*r, y2], 90, 180, fill=fill)
    d.pieslice([x2-2*r, y2-2*r, x2, y2], 0, 90, fill=fill)
    if outline:
        d.arc([x1, y1, x1+2*r, y1+2*r], 180, 270, fill=outline)
        d.arc([x2-2*r, y1, x2, y1+2*r], 270, 360, fill=outline)
        d.arc([x1, y2-2*r, x1+2*r, y2], 90, 180, fill=outline)
        d.arc([x2-2*r, y2-2*r, x2, y2], 0, 90, fill=outline)
        d.line([x1+r, y1, x2-r, y1], fill=outline)
        d.line([x1+r, y2, x2-r, y2], fill=outline)
        d.line([x1, y1+r, x1, y2-r], fill=outline)
        d.line([x2, y1+r, x2, y2-r], fill=outline)

def draw_screw(cx, cy, r=3):
    draw.ellipse([cx-r, cy-r, cx+r, cy+r], fill=SCREW, outline=SCREW_HOLE)
    draw.line([cx-1, cy-1, cx+1, cy+1], fill=SCREW_HOLE)

def draw_speaker(cx, cy, outer_r=38):
    draw.ellipse([cx-outer_r-3, cy-outer_r-3, cx+outer_r+3, cy+outer_r+3],
                 fill=SPEAKER_DARK, outline=BODY_DARK)
    for i in range(outer_r, 4, -3):
        c = SPEAKER_MID if (outer_r - i) % 6 < 3 else SPEAKER_RING
        draw.ellipse([cx-i, cy-i, cx+i, cy+i], fill=c)
    draw.ellipse([cx-8, cy-8, cx+8, cy+8], fill=SPEAKER_HIGHLIGHT)
    draw.ellipse([cx-6, cy-6, cx+6, cy+6], fill=SPEAKER_RING)
    draw.ellipse([cx-3, cy-3, cx+3, cy+3], fill=SPEAKER_DARK)
    for angle_deg in range(0, 360, 18):
        for dist in range(12, outer_r - 4, 6):
            ax = cx + int(dist * math.cos(math.radians(angle_deg)))
            ay = cy + int(dist * math.sin(math.radians(angle_deg)))
            draw.ellipse([ax-1, ay-1, ax, ay], fill=SPEAKER_DARK)

# Widget layout constants (must match Java code)
WX = 80          # widget X offset in texture
WY = 55          # first widget Y offset
WWIDTH = 200     # widget area width

# Cassette deck bounds (wraps around widgets with padding)
DECK_X1 = WX - 6
DECK_X2 = WX + WWIDTH + 6
DECK_Y1 = 40
DECK_Y2 = 190

# Display panel bounds
DISP_X1 = WX - 10
DISP_X2 = WX + WWIDTH + 10
DISP_Y1 = 200
DISP_Y2 = 248

# Speaker centers (in the space outside the deck)
SPK_L_CX = DECK_X1 // 2
SPK_R_CX = DECK_X2 + (W - DECK_X2) // 2
SPK_CY = (DECK_Y1 + DECK_Y2) // 2

# === MAIN BOOMBOX BODY ===
draw_rounded_rect(draw, 3, 28, W-4, H-4, 8, BODY_DARK, BODY_MID)
draw_rounded_rect(draw, 5, 30, W-6, H-6, 7, BODY_MID)
draw.line([12, 30, W-13, 30], fill=BODY_HIGHLIGHT)
draw.line([12, 31, W-13, 31], fill=BODY_LIGHT)

# === HANDLE ===
hx1, hx2 = W//2 - 70, W//2 + 70
draw_rounded_rect(draw, hx1+3, 18, hx1+18, 35, 3, HANDLE_DARK, BODY_DARK)
draw_rounded_rect(draw, hx2-18, 18, hx2-3, 35, 3, HANDLE_DARK, BODY_DARK)
draw_rounded_rect(draw, hx1, 8, hx2, 22, 6, HANDLE_DARK, BODY_DARK)
draw_rounded_rect(draw, hx1+2, 10, hx2-2, 19, 5, HANDLE_LIGHT)
draw_rounded_rect(draw, hx1+4, 11, hx2-4, 17, 4, HANDLE_DARK)
for x in range(hx1+12, hx2-10, 4):
    draw.line([x, 12, x, 16], fill=BODY_DARK)

# === SPEAKERS ===
draw_speaker(SPK_L_CX, SPK_CY)
draw_speaker(SPK_R_CX, SPK_CY)

# === CASSETTE DECK ===
draw.rectangle([DECK_X1-2, DECK_Y1-2, DECK_X2+2, DECK_Y2+2], fill=BODY_DARK)
draw.rectangle([DECK_X1, DECK_Y1, DECK_X2, DECK_Y2], fill=CASSETTE_BG, outline=CASSETTE_BORDER)

# --- Recessed areas for each widget row ---
# URL tape window (y=55-75)
uw_y1, uw_y2 = WY - 3, WY + 22
draw.rectangle([WX-3, uw_y1, WX+WWIDTH+3, uw_y2], fill=RECESS, outline=RECESS_BORDER)
# Tape reel decorations inside window
rc_y = (uw_y1 + uw_y2) // 2
draw.ellipse([WX+2, rc_y-7, WX+16, rc_y+7], outline=(50, 45, 58))
draw.ellipse([WX+5, rc_y-4, WX+13, rc_y+4], outline=(40, 36, 48))
draw.ellipse([WX+WWIDTH-16, rc_y-7, WX+WWIDTH-2, rc_y+7], outline=(50, 45, 58))
draw.ellipse([WX+WWIDTH-13, rc_y-4, WX+WWIDTH-5, rc_y+4], outline=(40, 36, 48))
draw.line([WX+14, uw_y1+2, WX+WWIDTH-14, uw_y1+2], fill=(50, 45, 58))
draw.line([WX+14, uw_y2-2, WX+WWIDTH-14, uw_y2-2], fill=(50, 45, 58))

# Button area (y=85-105)
btn_y1, btn_y2 = WY + 27, WY + 52
draw.rectangle([WX-3, btn_y1, WX+WWIDTH+3, btn_y2], fill=RECESS, outline=RECESS_BORDER)

# Volume slider groove (y=113-133)
vol_y1, vol_y2 = WY + 55, WY + 80
draw.rectangle([WX-3, vol_y1, WX+WWIDTH+3, vol_y2], fill=RECESS, outline=RECESS_BORDER)
# Thin track line
draw.line([WX+5, (vol_y1+vol_y2)//2, WX+WWIDTH-5, (vol_y1+vol_y2)//2], fill=(45, 42, 52))

# Range slider groove (y=137-157)
rng_y1, rng_y2 = WY + 79, WY + 104
draw.rectangle([WX-3, rng_y1, WX+WWIDTH+3, rng_y2], fill=RECESS, outline=RECESS_BORDER)
draw.line([WX+5, (rng_y1+rng_y2)//2, WX+WWIDTH-5, (rng_y1+rng_y2)//2], fill=(45, 42, 52))

# Transport controls area (y=165-185)
trn_y1, trn_y2 = WY + 107, WY + 132
draw.rectangle([WX-3, trn_y1, WX+WWIDTH+3, trn_y2], fill=RECESS, outline=RECESS_BORDER)

# Cassette deck screws
draw_screw(DECK_X1+4, DECK_Y1+4)
draw_screw(DECK_X2-4, DECK_Y1+4)
draw_screw(DECK_X1+4, DECK_Y2-4)
draw_screw(DECK_X2-4, DECK_Y2-4)

# Label/brand badge area
badge_x1 = W//2 - 35
badge_x2 = W//2 + 35
draw.rectangle([badge_x1, DECK_Y1+1, badge_x2, DECK_Y1+10], fill=(38, 35, 42), outline=CASSETTE_BORDER)

# === RED ACCENT STRIPE ===
draw.rectangle([5, 193, W-6, 196], fill=ACCENT)
draw.rectangle([6, 194, W-7, 195], fill=(200, 80, 70))

# === DISPLAY PANEL ===
draw.rectangle([DISP_X1-1, DISP_Y1-1, DISP_X2+1, DISP_Y2+1], fill=DISPLAY_BORDER)
draw.rectangle([DISP_X1, DISP_Y1, DISP_X2, DISP_Y2], fill=DISPLAY_BG)
for sy in range(DISP_Y1 + 2, DISP_Y2, 2):
    draw.line([DISP_X1+1, sy, DISP_X2-1, sy], fill=(20, 25, 17))
# Track info section
draw.rectangle([DISP_X1+2, DISP_Y1+3, DISP_X2-2, DISP_Y1+17], outline=(30, 40, 25))
# Status section
draw.rectangle([DISP_X1+2, DISP_Y1+20, DISP_X2-2, DISP_Y1+32], outline=(30, 40, 25))
# Error section
draw.rectangle([DISP_X1+2, DISP_Y1+35, DISP_X2-2, DISP_Y1+46], outline=(30, 35, 25))

# === LEDs ===
led_x_l = DISP_X1 - 12
led_x_r = DISP_X2 + 8
draw.ellipse([led_x_l, 203, led_x_l+4, 207], fill=(50, 200, 50))
draw.ellipse([led_x_l, 215, led_x_l+4, 219], fill=(200, 50, 50))
draw.ellipse([led_x_l, 227, led_x_l+4, 231], fill=(200, 200, 50))
draw.ellipse([led_x_r, 203, led_x_r+4, 207], fill=(50, 200, 50))
draw.ellipse([led_x_r, 215, led_x_r+4, 219], fill=(200, 50, 50))
draw.ellipse([led_x_r, 227, led_x_r+4, 231], fill=(200, 200, 50))

# === CORNER SCREWS ===
draw_screw(15, 38)
draw_screw(W-16, 38)
draw_screw(15, H-15)
draw_screw(W-16, H-15)

# === BOTTOM FEET ===
draw_rounded_rect(draw, 25, H-8, 55, H-3, 2, BODY_DARK, (35, 32, 40))
draw_rounded_rect(draw, W//2-20, H-8, W//2+20, H-3, 2, BODY_DARK, (35, 32, 40))
draw_rounded_rect(draw, W-56, H-8, W-26, H-3, 2, BODY_DARK, (35, 32, 40))

# === VENTILATION SLOTS ===
for vy in range(160, 185, 4):
    draw.line([8, vy, SPK_L_CX+20, vy], fill=BODY_DARK)
    draw.line([SPK_R_CX-20, vy, W-9, vy], fill=BODY_DARK)

# Cyan accent
draw.line([DECK_X1, 195, DECK_X2, 195], fill=ACCENT2)

out = r"e:\Modding\Soundscape\src\main\resources\assets\soundscape\textures\gui\boombox.png"
img.save(out)
print(f"Boombox GUI saved: {W}x{H}")

# === TEMPLATE OVERLAY ===
template = img.copy()
td = ImageDraw.Draw(template)

def tbox(x, y, w, h, label, col=(255, 255, 0, 120)):
    td.rectangle([x, y, x+w-1, y+h-1], outline=col)
    try:
        tw = td.textlength(label, font=font_sm)
    except:
        tw = len(label) * 5
    td.text((x + (w - tw) // 2, y + (h - 7) // 2), label, fill=(255, 255, 0, 255), font=font_sm)

tbox(WX, WY, 200, 20, "URL TEXT FIELD")
tbox(WX, WY+30, 63, 20, "PLAY")
tbox(WX+68, WY+30, 63, 20, "PAUSE")
tbox(WX+136, WY+30, 64, 20, "STOP")
tbox(WX, WY+58, 200, 20, "VOLUME SLIDER")
tbox(WX, WY+82, 200, 20, "RANGE SLIDER")
tbox(WX, WY+110, 80, 20, "LOOP")
tbox(WX+84, WY+110, 25, 20, "<<")
tbox(WX+112, WY+110, 30, 20, "-15s")
tbox(WX+145, WY+110, 30, 20, "+15s")
tbox(WX+178, WY+110, 22, 20, ">>")
tbox(DISP_X1+5, DISP_Y1+3, DISP_X2-DISP_X1-10, 14, "TRACK INFO", (100, 255, 100, 200))
tbox(DISP_X1+5, DISP_Y1+20, DISP_X2-DISP_X1-10, 12, "STATUS", (100, 255, 100, 200))
tbox(DISP_X1+5, DISP_Y1+35, DISP_X2-DISP_X1-10, 11, "ERROR", (255, 100, 100, 200))
tbox(badge_x1+2, DECK_Y1+2, badge_x2-badge_x1-4, 8, "TITLE", (255, 200, 100, 200))

tmpl_out = r"e:\Modding\Soundscape\src\main\resources\assets\soundscape\textures\gui\boombox_template.png"
template.save(tmpl_out)
print(f"Template saved: {W}x{H}")
