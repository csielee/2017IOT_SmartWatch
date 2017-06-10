/* 
 *  add u8glib library 
 */
#include "U8glib.h"
/*
 * choose SSD1306 chip by 128*64 pixel
 * and use I2C
 */
U8GLIB_SSD1306_128X64 u8g(U8G_I2C_OPT_NONE|U8G_I2C_OPT_DEV_0);

/*
 * time display
 */
void OLED_drawtime(const char *hourminute,const char *second) {
  // draw loop
  u8g.firstPage();
  do {
  // use osb18 Font
  u8g.setFont(u8g_font_osb18);
  // set the draw inital point at left top
  u8g.setFontPosTop();
  // get the string width
  int w = u8g.getStrWidth(hourminute);
  // draw hour and minute string in center
  u8g.drawStr( (u8g.getWidth() - w)/2, u8g.getHeight()/2 - 9, hourminute);
  // use fur11 Font
  u8g.setFont(u8g_font_fur11);
  // set the draw inital point at left top
  u8g.setFontPosTop();
  // draw second string in right
  u8g.drawStr((u8g.getWidth() + w)/2, u8g.getHeight()/2-1,second);
  } while( u8g.nextPage() );
}

void OLED_drawhrv(const char *bpm) {
  // draw loop
  u8g.firstPage();
  do {
  // use fur11 Font
  u8g.setFont(u8g_font_fur11);
  // set the draw inital point at left top
  u8g.setFontPosTop();
  // get the string width
  int w = u8g.getStrWidth(bpm);
  // draw bpm in center
  u8g.drawStr((u8g.getWidth() - w)/2, u8g.getHeight()/2 - 5,bpm);
  } while( u8g.nextPage() );
}

void OLED_drawstep(const char *altitude) {
  // draw loop
  u8g.firstPage();
  do {
  // use fur11 Font
  u8g.setFont(u8g_font_fur11);
  // set the draw inital point at left top
  u8g.setFontPosTop();
  // get the string width
  int w = u8g.getStrWidth(altitude);
  // draw altitude in center
  u8g.drawStr((u8g.getWidth() - w)/2, u8g.getHeight()/2 - 5,altitude);
  } while( u8g.nextPage() );
}

void OLED_init(void) {
  // assign default color value
  if ( u8g.getMode() == U8G_MODE_R3G3B2 ) {
    u8g.setColorIndex(255);     // white
  }
  else if ( u8g.getMode() == U8G_MODE_GRAY2BIT ) {
    u8g.setColorIndex(3);         // max intensity
  }
  else if ( u8g.getMode() == U8G_MODE_BW ) {
    u8g.setColorIndex(1);         // pixel on
  }
  else if ( u8g.getMode() == U8G_MODE_HICOLOR ) {
    u8g.setHiColorByRGB(255,255,255);
  }

}

