
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

/**
 * Application for map
 * @author student
 */
public class VD extends JFrame implements MouseListener, ActionListener, MouseMotionListener{
    /**
     * width and height of map
     */
    public int width = 60, height = 50;
    public Graphics g;
    private final Random rnd = new Random();

    // Step size for grid
    int stepX = 15, stepY = 15;

    // Error value to determine point clicking
    private final int errorX = 7, errorY = 7;

    private final int cityRadius = 12;

    private final int pointRadius = 4;

    // Velocity on roads
     double v = 2;

     List<Point> plist=new ArrayList<VD.Point>();
     /**
     * Holder for speeds.
     * Key is "x1-y1-x2-y2", assuming x1 < x2, if x1 = x2, then y1 < y2
     * Default value should be zero
     */
    private final Map<String, Double> speedMap = new HashMap<>();

    /**
     * Holder for cities with their position and color.
     */
    private final Map<Point, Color> cities = new HashMap<>();

    /**
     * Holder for cities with their position and color.
     */
    private final Map<Point, String> cityNames = new HashMap<>();

    /**
     * Boundary points for cities
     */
    private Map<Point, List<Point>> cityBoundaries = new HashMap<>();
   
    /**
     * Color for highways
     */
    private final Color colorHighway = new Color(0, 0, 255);

    /**
     * Shortest routes for all points
     */
    private final Map<PointPair, List<Point>> shortestRoutes = new HashMap<>();


    /**
     * Holders for starting and finishing point for dragging
     */
    private Point dragStart, draggedPoint;

    /**
     * position points
     */
//    private List<Point> positions = new ArrayList<>();

    int kk=3;
    /**
     * position point names
     */
//    private List<String> positionNames = new ArrayList<>();

    // store highways
    private final List<PointPair> highways = new ArrayList<>();

    // Constant for infinity distance
    private final double INF = 1e9;

    private final List<Color> colorList = List.of(new Color(1,0,103), new Color(213,255,0), new Color(255,0,86), new Color(158,0,142), new Color(14,76,161), new Color(255,229,2), new Color(0,95,57), new Color(0,255,0), new Color(149,0,58), new Color(255,147,126), new Color(164,36,0), new Color(0,21,68), new Color(145,208,203), new Color(98,14,0), new Color(107,104,130), new Color(0,0,255), new Color(0,125,181), new Color(106,130,108), new Color(0,174,126), new Color(194,140,159), new Color(190,153,112), new Color(0,143,156), new Color(95,173,78), new Color(255,0,0), new Color(255,0,246), new Color(255,2,157), new Color(104,61,59), new Color(255,116,163), new Color(150,138,232), new Color(152,255,82), new Color(167,87,64), new Color(1,255,254), new Color(255,238,232), new Color(254,137,0), new Color(189,198,255), new Color(1,208,255), new Color(187,136,0), new Color(117,68,177), new Color(165,255,210), new Color(255,166,254), new Color(119,77,0), new Color(122,71,130), new Color(38,52,0), new Color(0,71,84), new Color(67,0,44), new Color(181,0,255), new Color(255,177,103), new Color(255,219,102), new Color(144,251,146), new Color(126,45,210), new Color(189,211,147), new Color(229,111,254), new Color(222,255,116), new Color(0,255,120), new Color(0,155,255), new Color(0,100,1), new Color(0,118,255), new Color(133,169,0), new Color(0,185,23), new Color(120,130,49), new Color(0,255,198), new Color(255,110,65), new Color(232,94,190));

    // Controls
    JButton buttonShowDistance;
    JButton buttonShowShadow;
    JButton buttonPaintCity;
    JButton buttonCalculateDistance;
    JButton buttonDrawContour;
    JButton buttonDivideMap;
    JButton buttonAddHighway;
    JButton buttonClearHighway;
    JButton buttonSetGrid;
    JButton buttonSetHighwaySpeed;
    JButton buttonDrawHighway;
    JButton buttonSetK;


    public static void main(String[] args) {
        VD vd=new VD();
        vd.showvd();

    }

    public void showvd() {
        this.setTitle("Test");
        this.setSize(1250, 850);
        this.setLocation(50, 50);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setVisible(true);
        g = this.getGraphics();
        this.addMouseListener(this);    //鼠标监听
        this.addMouseMotionListener(this);
        this.setLayout(null);

        // Initialize controls
        int controlLeft = (width - 1) * stepX;
        buttonShowDistance = new JButton("Show distance between cities");
        this.add(buttonShowDistance);
        buttonShowDistance.setBounds(controlLeft, 60, 240, 30);
        buttonShowDistance.addActionListener(e -> drawCityDistance());

        buttonShowShadow = new JButton("Show shadow area");
        this.add(buttonShowShadow);
        buttonShowShadow.setBounds(controlLeft, 120, 150, 30);
        buttonShowShadow.addActionListener(e -> this.startShowShadow());

        buttonPaintCity = new JButton("Paint city area");
        this.add(buttonPaintCity);
        buttonPaintCity.setBounds(controlLeft, 90, 150, 30);
        buttonPaintCity.addActionListener(e -> {
//            this.updateCityBoundaries();
//            this.paintCities();
            this.paintCitiesPointwise();
            this.initializeMap();
        });

        buttonCalculateDistance = new JButton("Calculate distance from cities");
        this.add(buttonCalculateDistance);
        buttonCalculateDistance.setBounds(controlLeft, 0, 240, 30);
        buttonCalculateDistance.addActionListener(e -> {
        	ExecutorService executorService = Executors.newFixedThreadPool(cities.size()+20);
        	Future future = null;
            List<Thread> threadList = new ArrayList<>();
            try {
                long start = System.nanoTime();
                for (Point p : cities.keySet()) {
                    // Use threads for faster calculation
                    Thread t = new Thread(() -> drawContour(p, false));
                    t.start();
                    threadList.add(t);
                }

                for (Thread t: threadList) {
                    t.join();
                }
                

                long stop = System.nanoTime();

                JOptionPane.showMessageDialog(this, String.format("Process completed, time spent is %.0f seconds", (stop - start) / 1e9),
                        "Message", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        buttonDrawContour = new JButton("Draw contours for cities");
        this.add(buttonDrawContour);
        buttonDrawContour.setBounds(controlLeft, 30, 240, 30);
        buttonDrawContour.addActionListener(e -> {
            List<Thread> threadList = new ArrayList<>();
            try {
                for (Point p : cities.keySet()) {
                    // Use threads for faster calculation
                    Thread t = new Thread(() -> drawContour(p, true));
                    t.start();
                    threadList.add(t);
                }

				/*
				 * for (Thread t: threadList) { t.join(); }
				 */

                JOptionPane.showMessageDialog(this, "Process completed",
                        "Message", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        buttonDivideMap = new JButton("Divide map area");
        this.add(buttonDivideMap);
        buttonDivideMap.setBounds(controlLeft, 150, 150, 30);
        buttonDivideMap.addActionListener(e -> {
            try {
                this.showAllShadows();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        buttonAddHighway = new JButton("Add highway");
        this.add(buttonAddHighway);
        buttonAddHighway.setBounds(controlLeft, 180, 150, 30);
        buttonAddHighway.addActionListener(e -> {
            String coords = JOptionPane.showInputDialog(this,
                    "Please input starting point and end point of highway, format: x1,y1,x2,y2\n" +
                    "Maximum x: " + width + ", maximum y: " + height);

            try {
                String[] ps = coords.split(",");
                if (ps.length != 4) {
                    JOptionPane.showMessageDialog(this, "Invalid number of arguments", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int x1, y1, x2, y2;
                x1 = Integer.parseInt(ps[0]);
                y1 = Integer.parseInt(ps[1]);
                x2 = Integer.parseInt(ps[2]);
                y2 = Integer.parseInt(ps[3]);
                Point p1 = new Point(x1, y1), p2 = new Point(x2, y2);
                addRoad(p1, p2);
                initializeMap();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error during adding highways: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonClearHighway = new JButton("Remove all highways");
        this.add(buttonClearHighway);
        buttonClearHighway.setBounds(controlLeft, 210, 210, 30);
        buttonClearHighway.addActionListener(e -> {
            highways.clear();
            speedMap.clear();
            paint(g);
        });
        buttonSetGrid = new JButton("Set Grid");
        this.add(buttonSetGrid);
        buttonSetGrid.setBounds(controlLeft, 240, 210, 30);
        buttonSetGrid.addActionListener(e -> {
        	String str=JOptionPane.showInputDialog("please input size");
        	if(str!=null&&!str.equals("")) {
        		String dd[]=str.split(" ");
        		width=Integer.valueOf(dd[0]);
        		height=Integer.valueOf(dd[1]);
        	}
            paint(g);
        });
        buttonSetHighwaySpeed = new JButton("Set HighwaySpeed");
        this.add(buttonSetHighwaySpeed);
        buttonSetHighwaySpeed.setBounds(controlLeft, 270, 210, 30);
        buttonSetHighwaySpeed.addActionListener(e -> {
        	String str=JOptionPane.showInputDialog("please input speed");
        	if(str!=null&&!str.equals("")) {
        		v=Integer.valueOf(str);
        	}
        });
        buttonDrawHighway = new JButton("DrawHighway");
        this.add(buttonDrawHighway);
        buttonDrawHighway.setBounds(controlLeft, 300, 210, 30);
        buttonDrawHighway.addActionListener(e -> {
        	if(buttonDrawHighway.getText().equals("DrawHighway")) {
        		buttonDrawHighway.setText("DrawHighwaying");
        	}else {
        		buttonDrawHighway.setText("DrawHighway");
        	}
        });
        buttonSetK = new JButton("Set K");
        this.add(buttonSetK);
        buttonSetK.setBounds(controlLeft, 330, 210, 30);
        buttonSetK.addActionListener(e -> {
        	String str=JOptionPane.showInputDialog("please input k");
        	if(str!=null&&!str.equals("")) {
        		kk=Integer.valueOf(str);
        	}
        });
        // Initial cities
        addCity(new Point(2, 2));
        addCity(new Point(10, 6));
        addCity(new Point(6, 13));
        addCity(new Point(15, 8));
        addCity(new Point(12, 11));
        addCity(new Point(4, 6));
        addCity(new Point(16, 3));
        addCity(new Point(19, 15));
        addCity(new Point(39, 45));
        addCity(new Point(9, 35));
        addCity(new Point(40, 12));
        addCity(new Point(19, 15));
        addCity(new Point(55, 45));
        addCity(new Point(5, 43));
        addCity(new Point(23, 33));
        addCity(new Point(32, 43));
        addCity(new Point(37, 6));
        addCity(new Point(48, 31));
        addCity(new Point(53, 25));
        addCity(new Point(31, 28));

        // add roads
        addRoad(new Point(1, 2), new Point(1, 8));
        addRoad(new Point(6, 10), new Point(6, 11));
        addRoad(new Point(13, 12), new Point(15, 12));
        addRoad(new Point(11, 2), new Point(15, 2));
        addRoad(new Point(10, 4), new Point(10, 12));
        addRoad(new Point(4, 6), new Point(12, 6));

        initializeMap();
    }

    public void addCity(Point position) {
        if (position.ix() >= width || position.iy() >= height) {
            // Do not allow adding point out of map
            return;
        }
        System.out.println("Stadtpunkt: " + position.x + "," + position.y);
        cities.put(position, randomColor());
        cityNames.put(position, Character.toString(97 + cityNames.size()));
    }

    public void addRoad(Point p1, Point p2) {
        if (p1.x == p2.x && p1.y == p2.y) {
            return;
        }
        if (p1.x > p2.x || (p1.x == p2.x && p1.y > p2.y)) {
            // Ensure order of parameters
            Point temp = p1;
            p1 = p2;
            p2 = temp;
        }
        if (p1.x == p2.x) {
            int x = p1.ix();
            assert p1.y >= p2.y;
            for (int y = p1.iy(); y < p2.iy(); y++) {
                drawLine(new Point(x, y), new Point(x, y+1), colorHighway);
                speedMap.putIfAbsent(String.format("%d-%d-%d-%d", x, y, x, y + 1), v);
            }
        } else if (p1.y == p2.y) {
            int y = p1.iy();
            assert p1.x >= p2.x;
            for (int x = p1.ix(); x < p2.ix(); x++) {
                drawLine(new Point(x, y), new Point(x + 1, y), colorHighway);
                speedMap.putIfAbsent(String.format("%d-%d-%d-%d", x, y, x + 1, y), v);
            }
        } else {
            return;
        }
        highways.add(PointPair.of(p1, p2));
    }

//    public void addPosition(Point position) {
//        positions.add(position);
//        positionNames.add(Character.toString(97 + positionNames.size()));
//    }

    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(Color.white);//背景
        g.drawRect(0, 0, 1300, 1300);
        initializeMap();
    }

    public void initializeMap() {
        g.setColor(Color.GRAY);
        for(int i = 0; i < width - 1; i++) {  //竖线
            g.drawLine(stepX*i,stepY,stepX*i, stepY * (height - 2));
        }
        for(int i = 0; i < height - 1; i++) {  //横线
            g.drawLine(stepX,stepY*i,stepX * (width - 2),stepY*i );
        }

        drawCities();
        drawRoads();
    }

//    private void drawPositions() {
//        for (int k = 0; k < positions.size(); k++) {
//            Point p = positions.get(k);
//            drawPoint(p.ix(), p.iy(), 10, Color.BLACK);
//            drawText(new Point(p.x - 0.2, p.y - 0.2), positionNames.get(k));
//        }
//    }

    private void drawCities() {
        for (Point c: cities.keySet()) {
            drawPoint(c.ix(), c.iy(), cityRadius * 2, cities.get(c));
            drawText(new Point(c.x - 0.7, c.y - 0.7), cityNames.get(c));
        }
    }


    private void drawRoads() {
        for (PointPair pp: highways) {
            drawRoad(pp.a, pp.b);
        }
    }

    public void drawPoint(int i, int j, int r, Color color) {
        g.setColor(color);
        g.fillOval(stepX * i - r / 2, stepY * j - r / 2, r, r);
    }

    /**
     * Draw text on the map
     * @param p point in the map
     * @param text string to draw on the map
     */
    public void drawText(Point p, String text) {
        g.setFont(new Font("Arial", Font.PLAIN, 17));
        g.drawString(text, (int) (p.x * stepX), (int) (p.y * stepY));
    }

    public void drawPoint(int i, int j, Color color) {
        drawPoint(i, j, cityRadius * 2, color);
    }

    /**
     * Draw and store a road between two points.
     * @param p1 first point
     * @param p2 second point
     */
    public void drawRoad(Point p1, Point p2) {
        drawLine(p1, p2, colorHighway);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Clear initial selections
        dragStart = null;
        draggedPoint = null;
    }

    
    @Override
    public void mouseClicked(MouseEvent e) {
    	if(buttonDrawHighway.getText().equals("DrawHighwaying")) {
    		if(plist.size()==1) {
    			Point clickedPoint = getClickedPoint(e);
    			plist.add(clickedPoint);
    			if(plist!=null&&(plist.get(0).ix()==plist.get(1).ix()&&plist.get(0).iy()!=plist.get(1).iy())
    					||(plist.get(0).ix()!=plist.get(1).ix()&&plist.get(0).iy()==plist.get(1).iy())) {
    				addRoad(plist.get(0), plist.get(1));
    				initializeMap();
    			}
    			plist.clear();
    		}else {
    			Point clickedPoint = getClickedPoint(e);
    			plist.add(clickedPoint);
    		}
    		
    	}else {
    		Point clickedPoint = getClickedPoint(e);
            if (clickedPoint == null) {
                // No point is selected
                return;
            }
            if (!e.isConsumed() && e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
                // Single click: add city
                addCity(new Point(clickedPoint.ix(), clickedPoint.iy()));
                drawCities();
                e.consume();
            }
            if (!e.isConsumed() && e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                // Reset canvas
                paint(g);
                initializeMap();
                e.consume();
            }
            if (!e.isConsumed() && e.getButton() == MouseEvent.BUTTON3) {
                // Right click: show the shortest route
//                positions.add(clickedPoint);
//                positionNames.add(Character.toString(97 + positionNames.size()));
//                drawPositions();
//                System.out.println("Selected position: " + clickedPoint);
//                e.consume();
            }
    	}
        
    }

    /**
     * Draw distance of all cities on map
     */
    private void drawCityDistance() {
        for (Point p: cities.keySet()) {
            // Draw label for city
            drawText(new Point(p.x - 0.5, p.y + 0.5), cityNames.get(p));

//            if (p.equals(source)) {
//                return;
//            }

            // Generate distance strings
            StringBuilder distanceLabel = new StringBuilder(), distanceValueLabel = new StringBuilder();
            for (Point p2: cities.keySet()) {
                if (p.equals(p2)) {
                    continue;
                }
                distanceLabel.append(cityNames.get(p)).append("->").append(cityNames.get(p2)).append(", ");
                distanceValueLabel.append(getDistance(shortestRoutes.get(PointPair.of(p, p2)))).append(", ");
            }
            g.setColor(Color.black);
            drawText(new Point(p.x + 0.5, p.y - 0.5), distanceLabel.toString());
            drawText(new Point(p.x + 0.5, p.y), distanceValueLabel.toString());
        }
    }



    /**
     * Draw contours from a center point
     * @param center center point
     */
    private void drawContour(Point center, boolean doDrawing) {
        if (!cities.containsKey(center)) {
            // Not a city, no need to draw contours
            return;
        }
        List<Point> selected = new ArrayList<>(List.of(center));
//        List<Point> search = List.of(center);
        shortestRoutes.put(PointPair.of(center, center), List.of(center));
        double[][] distance = new double[width][height];
        // Dijkstra implemented here
        Map<Point, Double> distanceLocal;
        Map<Point, List<Point>> routeLocal;
        while (selected.size() < width * height) {
            // System.out.printf("Selected %d points.%n", selected.size());
            List<Point> neighbors = getNeighborsExclusive(selected);
            if (neighbors.size() < 1) {
                break;
            }
            // System.out.printf("Determining distance for %d points.%n", neighbors.size());
            distanceLocal = new HashMap<>();
            routeLocal = new HashMap<>();
            for (Point p: neighbors) {
                for (Point s : selected) {
                    // Only test distance for adjacent points
                    if (!(Math.abs(p.ix() - s.ix()) == 1 && Math.abs(p.iy() - s.iy()) == 0)
                            && !(Math.abs(p.ix() - s.ix()) == 0 && Math.abs(p.iy() - s.iy()) == 1)) {
                        continue;
                    }
                    double tryDistance = getDistance(p, s) + getDistance(shortestRoutes.get(PointPair.of(s, center)));
                    if (tryDistance > 0) {
                        Double currentDistance = distanceLocal.getOrDefault(p, INF);
                        if (tryDistance < currentDistance) {
                            distanceLocal.put(p, tryDistance);
                            List<Point> newRoute = new ArrayList<>(shortestRoutes.get(PointPair.of(s, center)));
                            newRoute.add(p);
                            routeLocal.put(p, newRoute);
                        }
                    }
                }
            }
            // Get point with minimum value in distanceLocal
            double d = INF;
            Point closest = neighbors.get(0);
            for (Point p: neighbors) {
                if (distanceLocal.get(p) < d) {
                    closest = p;
                    d = distanceLocal.get(p);
                }
            }
            selected.add(closest);
            distance[closest.ix()][closest.iy()] = d;
            shortestRoutes.put(PointPair.of(closest, center), routeLocal.get(closest));
//                selected.add(p);
//                List<Point> neighbors = getNeighbors(p);
//                for (Point p2: neighbors) {
//                    if (!selected.contains(p2) && !newSearch.contains(p2)) {
//                        // Add point to next search queue
//                        newSearch.add(p2);
//                        // Add route for this point
//                        List<Point> currentRoute = shortestRoutes.get(PointPair.of(center, p2)),
//                                newRoute = new ArrayList<>(shortestRoutes.get(PointPair.of(center, p)));
//                        double currentDistance = INF;
//                        if (currentRoute != null) {
//                            currentDistance = getDistance(currentRoute);
//                        }
//                        newRoute.add(p2);
//                        double newDistance = getDistance(newRoute);
//                        if (newDistance < currentDistance) {
//                            shortestRoutes.put(PointPair.of(center, p2), newRoute);
//                            distance[p2.ix()][p2.iy()] = newDistance;
//                            // Draw point distance, DEBUG ONLY
////                            g.setColor(Color.black);
////                            g.drawString(String.format("%.1f", distance[p2.ix()][p2.iy()]), p.ix() * stepX, p.iy() * stepY);
//                        }
//                    }
//                }
//            }
//            search = newSearch;
        }
        if (doDrawing) {
            InterpolatedGrid grid = new InterpolatedGrid(distance, stepX, stepY, 0.01);
            drawContours(grid);
        }
        // drawContours(distance);
    }

    /**
     * Get distance of given route
     * @param route List of points for route
     * @return total distance
     */
    private double getDistance(List<Point> route) {
        if (route == null) {
            return INF;
        }
        if (route.size() < 2) {
            return 0.0;
        }
        double distance = 0.0;
        for (int k = 0; k < route.size() - 1; k++) {
            Point p1 = route.get(k), p2 = route.get(k+1);
            distance += getDistance(p1, p2);
        }
        return distance;
    }

    private double getDistance(Point p1, Point p2) {
        if (p1.x > p2.x || (Double.compare(p1.x, p2.x) == 0 && p1.y > p2.y)) {
            // Ensure order of parameters
            Point temp = p1;
            p1 = p2;
            p2 = temp;
        }
        String key = String.format("%d-%d-%d-%d", p1.ix(), p1.iy(), p2.ix(), p2.iy());
        return 1.0 / speedMap.getOrDefault(key, 1.0);  // Distance is 1/v
    }

    /**
     * Draw contours based on the distance map.
     * @param distanceMap interpolated distance for the whole map used for contour drawing
     */
    private void drawContours(InterpolatedGrid distanceMap) {
        int distance = 1;
        List<Point> points = distanceMap.getPoints(distance);
        while (points.size() > 0) {
            drawSingleContour(points);
            distance += 1;
            points = distanceMap.getPoints(distance);
        }
    }

    private boolean pointisOutside(Point point) {
        int t = 1;
        return (point.x < t || point.x > width - t - 1 || point.y < t || point.y > height - t - 1);
    }

    private void drawSingleContour(List<Point> points) {
        Color color = randomColor();
        if (points.size() < 2) {
            // Only one point, no need to draw contour
            return;
        }
        points = sortPointsClosest(points);
        for (int i = 0; i < points.size(); i++) {
            Point p1 = points.get(i), p2 = points.get((i + 1) % points.size());
            if (pointisOutside(p1) || pointisOutside(p2)) {
                // Ignore residual lines crossing outside points
                continue;
            }
            if (Math.abs(p1.x - p2.x) <= 1 || Math.abs(p1.y - p2.y) <= 1) {
                drawLine(p1.x, p1.y, p2.x, p2.y, color);
            }
        }
    }

    /**
     * Sort the points in a list, so that they form a polygon.
     * @param points points to sort
     * @return sorted list of points
     */
    private List<Point> sortPointsClosest(List<Point> points) {
        List<Point> result = new ArrayList<>();
        int size = points.size();
        result.add(points.remove(0));
        for (int i = 1; i < size; i++) {
            Point p1 = result.get(result.size() - 1);
            double minDistance = 1e9;
            Point closest = null;
            for (Point p2: points) {
                double distance = Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
                if (distance < minDistance) {
                    minDistance = distance;
                    closest = p2;
                }
            }
            result.add(points.remove(points.indexOf(closest)));
        }
        return result;
    }

    /**
     * Sort the points in a list, so that they form a polygon.
     * @param points points to sort
     * @return sorted list of points
     */
    private List<Point> sortPointsClockwise(List<Point> points) {
        double xc = 0, yc = 0;
        for (Point p: points) {
            xc += p.x;
            yc += p.y;
        }
        xc /= points.size();
        yc /= points.size();
        double finalYc = yc;
        double finalXc = xc;
        List<Point> result = points.stream()
                .sorted(Comparator.comparing(p -> Math.atan2(p.y - finalYc, p.x - finalXc)))
                .collect(Collectors.toList());
        return result;
    }

    /**
     * Draw a line between two points in the grid.
     * @param p1 first point
     * @param p2 second point
     * @param color color for the line
     */
    private void drawLine(Point p1, Point p2, Color color) {
        g.setColor(color);
        if (color.equals(colorHighway)) {
            // Add offset for highways
    		g.drawLine(p1.ix() * stepX , p1.iy() * stepY , p2.ix() * stepX , p2.iy() * stepY );

        	for (int i = 1; i < 4; i++) {
        		g.drawLine(p1.ix() * stepX + i, p1.iy() * stepY + i, p2.ix() * stepX + i, p2.iy() * stepY + i);
                g.drawLine(p1.ix() * stepX - i, p1.iy() * stepY + i, p2.ix() * stepX - i, p2.iy() * stepY + i);
                g.drawLine(p1.ix() * stepX + i, p1.iy() * stepY - i, p2.ix() * stepX + i, p2.iy() * stepY - i);
                g.drawLine(p1.ix() * stepX - i, p1.iy() * stepY - i, p2.ix() * stepX - i, p2.iy() * stepY - i);
			}
            
        } else {
            drawPoint(p1.ix(), p1.iy(), pointRadius * 2, color);
            drawPoint(p2.ix(), p2.iy(), pointRadius * 2, color);
            g.drawLine(p1.ix() * stepX, p1.iy() * stepY, p2.ix() * stepX, p2.iy() * stepY);
        }
    }


    private void drawLine(double x1, double y1, double x2, double y2, Color color) {
        g.setColor(color);
        if (color.equals(colorHighway)) {
            // Add offset for highways
            g.drawLine((int) (x1 * stepX + 1), (int) (y1 * stepY + 1), (int) (x2 * stepX + 1), (int) (y2 * stepY + 1));
        } else {
            g.drawLine((int) (x1 * stepX), (int) (y1 * stepY), (int) (x2 * stepX), (int) (y2 * stepY));
        }
    }

    /**
     * Get neighbor points of given point
     * @param p point to search for neighbors
     * @return list of neighbor points
     */
    private List<Point> getNeighbors(Point p) {
        int x = p.ix(), y = p.iy();
        List<Point> points = new ArrayList<>();
        if (x > 0) {
            points.add(new Point(x - 1, y));
        }
        if (y > 0) {
            points.add(new Point(x, y - 1));
        }
        if (x + 1 < width) {
            points.add(new Point(x + 1, y));
        }
        if (y + 1 < height) {
            points.add(new Point(x, y + 1));
        }
        return points;
    }



    /**
     * Get neighbor points of given points, the points in the list is excluded
     * @param points points to search for neighbors
     * @return list of neighbor points
     */
    private List<Point> getNeighborsExclusive(List<Point> points) {
        List<Point> result = new ArrayList<>();

        for (Point p: points) {
            result.addAll(getNeighbors(p));
        }

        return result.stream().distinct().filter((p) -> !points.contains(p)).collect(Collectors.toList());
    }

    /**
     * Get the coordinate of the clicked point in the grid.
     * @param e Mouse click event
     * @return Clicked point
     */
    private Point getClickedPoint(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        int nx = x / stepX;
        int ny = y / stepY;
        int residueX = x - (x / stepX * stepX);
        int residueY = y - (y / stepY * stepY);
        if ((residueX < errorX || residueX > stepX - errorX)
                && (residueY < errorY || residueY > stepY - errorY)) {
            return new Point(nx + (residueX > errorX ? 1 : 0), ny + (residueY > errorY ? 1 : 0));
        } else {
            // No valid point is clicked
            return null;
        }
    }

    /**
     * Get random color, limits are applied to the RGB values, so that the color won't be too white.
     * @return Random color
     */
    private Color randomColor() {
        return new Color(rnd.nextInt(240), rnd.nextInt(240), rnd.nextInt(240));
    }

    @Override
    public void mouseEntered(MouseEvent e){
    }
    @Override
    public void mouseExited(MouseEvent e){}
    @Override
    public void mouseMoved(MouseEvent e){}
    @Override
    public void mousePressed(MouseEvent e){
    	if(buttonDrawHighway.getText().equals("DrawHighwaying")) {
    		
    	}else {
    		Point clickedPoint = getClickedPoint(e);
            if (clickedPoint == null || !cities.containsKey(clickedPoint)) {
                // No point is clicked, or clicked point is not a city
                return;
            }
            dragStart = new Point(e.getX(), e.getY());
            draggedPoint = clickedPoint;
    	}
        
    }

    @Override
    public void mouseDragged(MouseEvent e){
    	if(buttonDrawHighway.getText().equals("DrawHighwaying")) {
    		
    	}else {
    		if (dragStart == null || draggedPoint == null) {
                // No starting point
                return;
            }
            double dx = e.getX() - this.dragStart.x;
            double dy = e.getY() - this.dragStart.y;
            if (Math.abs(dx) > stepX * 0.75 || Math.abs(dy) > stepY * 0.75) {
                Point newPoint = getClickedPoint(e);
                if (newPoint == null) {
                    return;
                }
                int newX = newPoint.ix();
                int newY = newPoint.iy();
                cities.put(newPoint, cities.remove(draggedPoint));
                cityNames.put(newPoint, cityNames.remove(draggedPoint));
                // Update starting conditions
                draggedPoint = new Point(newX, newY);
                dragStart = new Point(e.getX(), e.getY());
                // Update canvas
                paint(g);
            }
    	}
        
    }

    private Polygon getPolygon(List<Point> points) {
        // Check for outside points
        List<Point> points2 = new ArrayList<>();
        for (int k = 0; k < points.size(); k++) {
            Point p2 = points.get((k + 1) % points.size());
            if (!pointisOutside(p2)) {
                points2.add(p2);
            } else {
                // Move outside points to border
                int t = 1;
                Point p2p = new Point(0, 0);
                p2p.x = Math.min(Math.max(p2.x, t), width - t);
                p2p.y = Math.min(Math.max(p2.y, t), height - t);
                points2.add(p2);
            }
        }

        // Construct polygon
        Polygon plg = new Polygon();
        for (Point p: points2) {
            plg.addPoint((int) (p.x * stepX), (int) (p.y * stepY));
        }
        return plg;
    }
    /**
     * Paint colors for all cities
     */
    private void paintCities() {
        for (Map.Entry<Point, Color> city: cities.entrySet()) {
            Color color = city.getValue();
            Point cp = city.getKey();

            List<Point> vertices = cityBoundaries.get(cp);
            g.setColor(inverseColor(color));
            g.fillPolygon(getPolygon(vertices));

            drawPoint(cp.ix(), cp.iy(), color);
        }
//        for (Map.Entry<Point, Color> city: cities.entrySet()) {
//            Color color = city.getValue();
//            Point cp = city.getKey();
//        }
    }


    /**
     * Paint colors for all cities
     */
    private void paintCitiesPointwise() {
        Map<Point, List<Point>> cityRanges = new HashMap<>();
        // Calculate ranges
        int t = 1;
        for (int k = t; k < width - t; k++) {
            for (int l = t; l < height - t; l++) {
                Point pos = new Point(k, l);
                Point closestCity = cities.entrySet().stream()
                        .sorted(Comparator.comparingDouble(entry -> Math.pow(entry.getKey().x - pos.x, 2) + Math.pow(entry.getKey().y - pos.y, 2)))
                        .limit(1)
                        .map(Map.Entry::getKey).findFirst().orElse(null);
                cityRanges.putIfAbsent(closestCity, new ArrayList<>());
                cityRanges.get(closestCity).add(pos);
            }
        }

        // Paint cities
        for (Map.Entry<Point, Color> city: cities.entrySet()) {
            Color color = city.getValue();
            Point cp = city.getKey();

            List<Point> vertices = cityRanges.get(cp);
            for (Point v: vertices) {
                if (v.ix() >= width - t - 1 || v.iy() >= height - t - 1) {
                    continue;
                }
                g.setColor(inverseColor(color));
                g.fillRect((int) ((v.ix() - 0.) * stepX), (int) ((v.iy() - 0.) * stepY), stepX, stepY);
            }
        }
    }

    private Color inverseColor(Color c) {
        return new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue());
    }

//    /**
//     * Find boundary points between cities
//     */
//    private void updateCityBoundaries() {
//        // Step 1 triangulation
//        List<Vector2D> pointList = cities.keySet().stream()
//                .map(p -> new Vector2D(p.x, p.y))
//                .collect(Collectors.toList());
//        DelaunayTriangulator triangulator = new DelaunayTriangulator(pointList);
//        try {
//            triangulator.shuffle();
//            triangulator.triangulate();
//        } catch (NotEnoughPointsException e) {
//            e.printStackTrace();
//            return;
//        }
//        List<Triangle2D> triangleSoup = triangulator.getTriangles();
//        List<Triangle> triangles = triangleSoup.stream()
//                .map(t -> Triangle.of(new Point(t.a.x, t.a.y), new Point(t.b.x, t.b.y), new Point(t.c.x, t.c.y)))
//                .collect(Collectors.toList());
//
//        // Step 2: group the triangles by the vertices
//        // calculate the circumcenters, and generate Voronoi diagram
//        Map<Point, List<Triangle>> triangleMap = new HashMap<>();
//        for (Triangle t: triangles) {
//            for (Point p: List.of(t.a, t.b, t.c)) {
//                if (triangleMap.containsKey(p)) {
//                    triangleMap.get(p).add(t);
//                } else {
//                    List<Triangle> ts = new ArrayList<>();
//                    ts.add(t);
//                    triangleMap.put(p, ts);
//                }
//            }
//        }
//
//        // Step 3: find the circumcenters for triangles around one point
//        cityBoundaries = new HashMap<>();
//        for (Map.Entry<Point, List<Triangle>> entry: triangleMap.entrySet()) {
//            List<Point> vertexList = entry.getValue().stream()
//                    .map(this::getCircumCenter)
//                    .collect(Collectors.toList());
//            cityBoundaries.put(entry.getKey(), sortPointsClockwise(vertexList));
//        }
//    }

    /**
     * Get circumcenter of a triangle
     * @param triangle triangle to calculate
     * @return Point of circumcenter
     */
    private Point getCircumCenter(Triangle triangle) {
        Point a = triangle.a, b = triangle.b, c = triangle.c;
        double c1 = b.x * b.x + b.y * b.y - a.x * a.x - a.y * a.y;
        double c2 = c.x * c.x + c.y * c.y - a.x * a.x - a.y * a.y;
        double a1 = -2 * a.x + 2 * b.x;
        double b1 = -2 * a.y + 2 * b.y;
        double a2 = -2 * a.x + 2 * c.x;
        double b2 = -2 * a.y + 2 * c.y;

        double x = (c1 * b2 - c2 * b1) / (a1 * b2 - a2 * b1);
        double y = (a1 * c2 - a2 * c1) / (a1 * b2 - a2 * b1);
        return new Point(x, y);
    }

    @Override
    public void actionPerformed(ActionEvent e) {}

    public void startShowShadow() {
        if (shortestRoutes.size() < 1) {
            JOptionPane.showMessageDialog(this,
                    "You need to calculate distance from cities first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
//        // Check if it's needed to refresh boundary map
//        if (cityBoundaries.size() <= cities.size()) {
//            updateCityBoundaries();
//        }
        // Get cities
        String cityNameString = JOptionPane.showInputDialog(this,
                "Please input three city names, separated by commas");
        String[] cityNameList = cityNameString.split(",");
        if (cityNameList.length != 3) {
            JOptionPane.showMessageDialog(this,
                    "Wrong number of cities.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!cityNames.containsValue(cityNameList[0]) ||
                !cityNames.containsValue(cityNameList[1]) ||
                !cityNames.containsValue(cityNameList[2])) {
            JOptionPane.showMessageDialog(this,
                    "Non-existent city is specified.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Point cityA = null, cityB = null, cityC = null;
        for (Map.Entry<Point, String> entry: cityNames.entrySet()) {
            if (entry.getValue().equals(cityNameList[0])) {
                cityA = entry.getKey();
                continue;
            }
            if (entry.getValue().equals(cityNameList[1])) {
                cityB = entry.getKey();
                continue;
            }
            if (entry.getValue().equals(cityNameList[2])) {
                cityC = entry.getKey();
            }
        }

        List<Point> validPositions = new ArrayList<>();
        int t = 1;
        for (int k = t; k < width - t; k++) {
            for (int l = t; l < height - t; l++) {
                Point pos = new Point(k, l);
                List<Point> closestCities = getClosestCities(pos, 3);
                // System.out.printf("Closest cities for Position %s: %s%n", pos, closestCities);
                if (closestCities.contains(cityA) && closestCities.contains(cityB)
                        && closestCities.contains(cityC)) {
                    validPositions.add(pos);
                }
            }
        }

        List<Point> areaBoundary = sortPointsClockwise(validPositions);
        System.out.println("Points for area boundary: " + areaBoundary);
        if (areaBoundary.size() < 3) {
            JOptionPane.showMessageDialog(this, "Less than 3 points are closest to selected cities!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        for (Point p: areaBoundary) {
            drawPoint(p.ix(), p.iy(), pointRadius * 2, Color.BLACK);
        }
    }

    public void showAllShadows() {
        if (shortestRoutes.size() < 1) {
            JOptionPane.showMessageDialog(this,
                    "You need to calculate distance from cities first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Map<Integer, List<Point>> shadowMap = new HashMap<>();

        int t = 1;
        for (int k = t; k < width - t; k++) {
            for (int l = t; l < height - t; l++) {
                Point pos = new Point(k, l);
                int total=0;
                List<Point> closestCities = getClosestCities(pos, kk);
                // System.out.printf("Closest cities for Position %s: %s%n", pos, closestCities);
                for (int i = 0; i < closestCities.size(); i++) {
                	total+=closestCities.get(i).ix()+closestCities.get(i).iy();
				}
                if (!shadowMap.containsKey(total)) {
                    shadowMap.put(total, new ArrayList<>());
                }

                shadowMap.get(total).add(pos);
            }
        }

        int index = 0;
        for (List<Point> points: shadowMap.values()) {
            Color color = colorList.get(index % colorList.size());
            for (Point p: points) {
                drawPoint(p.ix(), p.iy(), pointRadius * 2, color);
            }
            index += 1;
        }
    }

    private List<Point> getClosestCities(Point position, int number) {
        Map<Point, Double> distanceMapLocal = cities.keySet().stream()
                .collect(Collectors.toMap(c -> c, c -> getCityDistance(c, position)));
        distanceMapLocal = distanceMapLocal.entrySet().stream()
                .sorted(Comparator.comparingDouble(Map.Entry<Point, Double>::getValue))
                .limit(number).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return List.copyOf(distanceMapLocal.keySet());
    }

    /**
     * Calculate closest distance between city and position
     * @param city Position of city
     * @param position Position to calculate
     * @return closest distance
     */
    private double getCityDistance(Point city, Point position) {
        List<Point> route = shortestRoutes.get(PointPair.of(position, city));
        return getDistance(route);
    }

    /**
     * Class for point supporting float coordinates
     */
    public static class Point {
        public double x;
        public double y;
        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Point point = (Point) o;

            if (Double.compare(point.x, x) != 0) return false;
            return Double.compare(point.y, y) == 0;
        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            temp = Double.doubleToLongBits(x);
            result = (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(y);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            return result;
        }

        public int ix() {
            return (int) Math.round(this.x);
        }

        public int iy() {
            return (int) Math.round(this.y);
        }

        @Override
        public String toString() {
            return "Point{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }
    }

    /**
     * Class for holding a pair of Point.
     * The order of points does not matter.
     */
    public static class PointPair {
        public Point a;
        public Point b;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PointPair pointPair = (PointPair) o;

            return (Objects.equals(b, pointPair.b) && Objects.equals(a, pointPair.a))
                    || (Objects.equals(b, pointPair.a) && Objects.equals(a, pointPair.b));
        }

        @Override
        public int hashCode() {
            int result = a != null ? a.hashCode() : 0;
            result = 31 * result + (b != null ? b.hashCode() : 0);
            return result;
        }

        public PointPair(Point a, Point b) {
            this.a = a;
            this.b = b;
        }

        public static PointPair of(Point a, Point b) {
            if (a.x > b.x || (Double.compare(a.x, b.x) == 0 && a.y > b.y)) {
                Point temp = a;
                a = b;
                b = temp;
            }
            return new PointPair(a, b);
        }

        @Override
        public String toString() {
            return "PointPair{" +
                    "a=" + a +
                    ", b=" + b +
                    '}';
        }
    }

    public static class InterpolatedGrid {
        private final Map<Integer, List<Point>> contours = new HashMap<>();
        public InterpolatedGrid(double[][] initialData, int scaleX, int scaleY, double error) {
            int x = initialData.length;
            int y = initialData[0].length;
            for (int i = 0; i < x - 1; i++) {
                for (int j = 0; j < y - 1; j ++) {
                    for (int sx = 0; sx < scaleX; sx++) {
                        for (int sy = 0; sy < scaleY; sy++) {
                            double value = (scaleX - sx) * (scaleY - sy) * initialData[i][j] +
                                    (sx) * (scaleY - sy) * initialData[i+1][j] +
                                    (scaleX - sx) * (sy) * initialData[i][j+1] +
                                    (sx) * (sy) * initialData[i+1][j+1];
                            value /= scaleX * scaleY;
                            if (value % 1.0 <= error) {
                                Integer index = (int) (value - value % 1.0);
                                List<Point> current = contours.getOrDefault(index, new ArrayList<>());
                                current.add(new Point(i + sx * 1.0 / scaleX, j + sy * 1.0 / scaleY));
                                contours.put(index, current);
                            }
                        }
                    }
                }
            }
        }

        public List<Point> getPoints(int distance) {
            for (Integer d: contours.keySet()) {
                if (d == distance) {
                    return contours.get(d);
                }
            }
            return List.of();
        }
    }

    /**
     * Represents a triangle in 2D space
     */
    public static class Triangle {
        private final Point a, b, c;

        public Triangle(Point a, Point b, Point c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        public static Triangle of(Point a, Point b, Point c) {
            return new Triangle(a, b, c);
        }

        public static Triangle of(PointPair ab, PointPair cd) {
            Point a = ab.a, b = ab.b;
            Point c = cd.a, d = cd.b;
            if (c.equals(a) || c.equals(b)) {
                return Triangle.of(a, b, d);
            } else if (d.equals(a) || d.equals(b)) {
                return Triangle.of(a, b, c);
            } else {
                return null;
            }
        }

        /**
         * tests if point p is within this triangle
         * adapted from https://blackpawn.com/texts/pointinpoly/
         * @param p point to test
         * @return whether the point is in this triangle
         */
        public boolean containPoint(Point p) {
            return sameSide(p, a, b, c) && sameSide(p, b, a, c) && sameSide(p, c, a, b);
        }

        /**
         * tests if point p is a vertex of this triangle
         * @param p point to test
         * @return whether the point is in this triangle
         */
        public boolean hasVertex(Point p) {
            return p.equals(a) || p.equals(b) || p.equals(c);
        }

        /**
         * Get the sides of this triangle
         * @return List of sides in this triangle
         */
        public List<PointPair> getSides() {
            return List.of(PointPair.of(a, b), PointPair.of(b, c), PointPair.of(c, a));
        }

        /**
         * Tests if point p1 and point p2 are on the same side of line ab
         * @param p1 Point to test
         * @param p2 Point to compare with
         * @param a first point on the line
         * @param b second point on the line
         * @return True if p1 and p2 are on the same side.
         */
        private boolean sameSide(Point p1, Point p2, Point a, Point b) {
            Point ba = new Point(b.x - a.x, b.y - a.y),
                    p1a = new Point(p1.x - a.x, p1.y - a.y),
                    p2a = new Point(p2.x - a.x, p2.y - a.y);
            double cp1 = ba.x * p1a.y - ba.y * p1a.x,
                    cp2 = ba.x * p2a.y - ba.y * p2a.x;
            return cp1 * cp2 >= 0;
        }
    }

    /**
     * Class for holding three Points.
     * The order of points does not matter.
     */
    public static class PointTriple {
        public Point a;
        public Point b;
        public Point c;

        public PointTriple(Point a, Point b, Point c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        public static PointTriple of(Point a, Point b, Point c) {
            List<Point> points = List.of(a, b, c).stream()
                    .sorted(Comparator.comparingDouble(p -> p.x * 1000 + p.y))
                    .collect(Collectors.toList());
            return new PointTriple(points.get(0), points.get(1), points.get(2));
        }

        public static PointTriple of(List<Point> points) {
            return PointTriple.of(points.get(0), points.get(1), points.get(2));
        }

        @Override
        public String toString() {
            return "PointTriple{" +
                    "a=" + a +
                    ", b=" + b +
                    ", c=" + c +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PointTriple that = (PointTriple) o;

            if (!Objects.equals(a, that.a)) return false;
            if (!Objects.equals(b, that.b)) return false;
            return Objects.equals(c, that.c);
        }

        @Override
        public int hashCode() {
            int result = a != null ? a.hashCode() : 0;
            result = 31 * result + (b != null ? b.hashCode() : 0);
            result = 31 * result + (c != null ? c.hashCode() : 0);
            return result;
        }
    }
}


    
