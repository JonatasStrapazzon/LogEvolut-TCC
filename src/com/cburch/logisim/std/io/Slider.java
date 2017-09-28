/*******************************************************************************
 * This file is part of logisim-evolution.
 *
 *   logisim-evolution is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   logisim-evolution is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with logisim-evolution.  If not, see <http://www.gnu.org/licenses/>.
 *
 *   Original code by Carl Burch (http://www.cburch.com), 2011.
 *   Subsequent modifications by :
 *     + Haute École Spécialisée Bernoise
 *       http://www.bfh.ch
 *     + Haute École du paysage, d'ingénierie et d'architecture de Genève
 *       http://hepia.hesge.ch/
 *     + Haute École d'Ingénierie et de Gestion du Canton de Vaud
 *       http://www.heig-vd.ch/
 *   The project is currently maintained by :
 *     + REDS Institute - HEIG-VD
 *       Yverdon-les-Bains, Switzerland
 *       http://reds.heig-vd.ch
 *******************************************************************************/

package com.cburch.logisim.std.io;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.instance.InstanceData;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstancePoker;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.tools.key.BitWidthConfigurator;
import com.cburch.logisim.util.GraphicsUtil;


public class Slider extends InstanceFactory {
    
	public static final int MAX_SLIDER = 8;
	public static final int MIN_SLIDER = 1;
        
        public static final int MIN_BIT = 1;
        public static final int MAX_BIT = 32;
    
        private static int WIDTH = 30;
        private static int HEIGHT = 100;
        
        private static int X_OFFSET = 5;
        
        private static int Cursor;
        
	public static class Poker extends InstancePoker {
		@Override
		public void mouseDragged(InstanceState state, MouseEvent e) {
			Location loc = state.getInstance().getLocation();
			int cx = loc.getX();
			int cy = loc.getY();
			updateState(state, e.getX() - cx, e.getY() - cy);
		}

		@Override
		public void mousePressed(InstanceState state, MouseEvent e) {
			mouseDragged(state, e);
		}

		@Override
		public void mouseReleased(InstanceState state, MouseEvent e) {
			//updateState(state, 0, 0);
		}

		@Override
		public void paint(InstancePainter painter) {
			State state = (State) painter.getData();
			if (state == null) {
				state = new State(0, 0);
				painter.setData(state);
			}
			Location loc = painter.getLocation();
			int x = loc.getX() + X_OFFSET;
			int y = loc.getY();
			Graphics g = painter.getGraphics();
			g.setColor(Color.WHITE);
			g.fillRect(x - (WIDTH-8)/2, y-(HEIGHT-8)/2, WIDTH-8, HEIGHT-8);
			GraphicsUtil.switchToWidth(g, 3);
			g.setColor(Color.BLACK);
                        g.drawLine(x, y-(HEIGHT-10)/2, x, y+(HEIGHT-10)/2);
			int dx = state.xPos;
			int dy = state.yPos;
			//int x0 = x + (dx > 5 ? 1 : dx < -5 ? -1 : 0);
			//int y0 = y + (dy > 5 ? 1 : dy < 0 ? -1 : 0);
			//int x1 = x + dx;
			//int y1 = y + dy;
			//g.drawLine(x0, y0, x1, y1);
			Color kbColor = painter.getAttributeValue(Io.ATTR_COLOR);
                        Slider.Cursor = y + dy;
			Slider.drawKnob(g, x, y + dy, kbColor, true);
		}

		private void updateState(InstanceState state, int dx, int dy) {
			State s = (State) state.getData();
			if (dy < -(HEIGHT-12)/2)
				dy = -(HEIGHT-12)/2;
			if (dy > (HEIGHT-12)/2)
				dy = (HEIGHT-12)/2;
			if (s == null) {
				s = new State(0, dy);
				state.setData(s);
			} else {
				s.xPos = dx;
				s.yPos = dy;
			}
			state.getInstance().fireInvalidated();
		}
	}

	private static class State implements InstanceData, Cloneable {
		private int xPos;
		private int yPos;

		public State(int x, int y) {
			xPos = x;
			yPos = y;
		}

		@Override
		public Object clone() {
			try {
				return super.clone();
			} catch (CloneNotSupportedException e) {
				return null;
			}
		}
	}

	private static void drawKnob(Graphics g, int x, int y, Color c,
			boolean inColor) {
            
		if (inColor) {
			g.setColor(c == null ? Color.RED : c);
		} else {
			int hue = c == null ? 128 : (c.getRed() + c.getGreen() + c
					.getBlue()) / 3;
			g.setColor(new Color(hue, hue, hue));
		}
		GraphicsUtil.switchToWidth(g, 1);
                g.fillRoundRect(x - WIDTH/3, y - 4, 2*WIDTH/3, 8, 3, 3);
		g.setColor(Color.BLACK);
                g.drawRoundRect(x - WIDTH/3, y - 4, 2*WIDTH/3, 8, 3, 3);
	}
        
	static final Attribute<BitWidth> ATTR_WIDTH = Attributes.forBitWidth(
			"bits", Strings.getter("ioBitWidthAttr"), MIN_BIT, MAX_BIT);

	static final Attribute<Integer> ATTR_SIZE = Attributes.forIntegerRange(
                        "number", Strings.getter("nrOfSliders"), MIN_SLIDER, MAX_SLIDER);
        
	public Slider() {
		super("Slider", Strings.getter("sliderComponent"));
                int sliderSize = 1;
                Cursor = 0;

		setAttributes(
                    new Attribute[] 
                    { 
                        StdAttr.LABEL, Io.ATTR_LABEL_LOC, StdAttr.LABEL_FONT, StdAttr.LABEL_COLOR, 
                        StdAttr.LABEL_VISIBILITY, ATTR_SIZE, ATTR_WIDTH, Io.ATTR_COLOR
                    },
		    new Object[] 
                    { 
                        "", Direction.EAST, StdAttr.DEFAULT_LABEL_FONT, StdAttr.DEFAULT_LABEL_COLOR, 
                        false, sliderSize, BitWidth.create(4), Color.RED 
                    }
                );
                
		setKeyConfigurator(new BitWidthConfigurator(ATTR_WIDTH, MIN_BIT, MAX_BIT));
		setOffsetBounds(Bounds.create(-(WIDTH/2)+X_OFFSET, -(HEIGHT/2), WIDTH, HEIGHT));
		setIconName("slider.gif");
		setPorts(new Port[] { new Port(WIDTH/2+X_OFFSET,  0, Port.OUTPUT, ATTR_WIDTH),
				      new Port(WIDTH/2+X_OFFSET, 10, Port.OUTPUT, ATTR_WIDTH), });
		setInstancePoker(Poker.class);
	}

	@Override
	public void paintGhost(InstancePainter painter) {
		Graphics g = painter.getGraphics();
		GraphicsUtil.switchToWidth(g, 2);
		g.drawRoundRect(-(WIDTH/2)+X_OFFSET, -(HEIGHT/2), WIDTH, HEIGHT, 8, 8);
	}

	@Override
	public void paintInstance(InstancePainter painter) {
		Location loc = painter.getLocation();
		int x = loc.getX() + X_OFFSET;
		int y = loc.getY();
                
                if(Cursor == 0) Cursor = y;

		Graphics g = painter.getGraphics();
		
                // border
                g.drawRoundRect(x - WIDTH/2, y - HEIGHT/2, WIDTH, HEIGHT, 8, 8);
		g.drawRoundRect(x - (WIDTH-4)/2, y - (HEIGHT-4)/2, WIDTH-4, HEIGHT-4, 4, 4);
                
                // rail
                GraphicsUtil.switchToWidth(g, 3);
                g.setColor(Color.BLACK);
                g.drawLine(x, y-(HEIGHT-10)/2, x, y+(HEIGHT-10)/2);
		
                // knob
                drawKnob(g, x, Cursor, painter.getAttributeValue(Io.ATTR_COLOR),
				painter.shouldDrawColor());
		painter.drawPorts();
	}

	@Override
	public void propagate(InstanceState state) {
		BitWidth bits = state.getAttributeValue(ATTR_WIDTH);
		int dy;
		State s = (State) state.getData();
		if (s == null) {
			dy = 0;
		} else {
			dy = -s.yPos;
		}

		int steps = (1 << bits.getWidth());
		dy = (dy + (HEIGHT-10)/2) * steps / (HEIGHT-10);
//		if (bits.getWidth() > 4) {
//			if (dx >= steps / 2)
//				dx++;
//			if (dy >= steps / 2)
//				dy++;
//		}
		state.setPort(0, Value.createKnown(bits, dy), 1);
		state.setPort(1, Value.createKnown(bits, dy), 1);
	}
}
