import javax.swing.JFrame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.awt.Color;
import java.awt.Font;
import java.awt.*;
import java.math.*;

class City{ //Xinyu Tu
    String name = "";
    int x = 0;
    int y = 0;
    static int redis = 14;
    int state = 0;
    boolean isRoad = false;

    double distance = Integer.MAX_VALUE;
    City parent = null;

    ArrayList<Edge> edges = new ArrayList<Edge>();

    Color color = Color.BLUE;

    public City(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int realX(int redis){
        return VD2.gridWidth + x * VD2.gridWidth - redis / 2;
    }

    public int realY(int redis){
        return VD2.gridheight + y * VD2.gridheight - redis / 2 + 15;
    }

    public int centerX(){
        return VD2.gridWidth + x * VD2.gridWidth;
    }

    public int centerY(){
        return VD2.gridheight + y * VD2.gridheight + 15;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof City){
            City c = (City)obj;
            if(c.x == this.x && c.y == this.y){
                return true;
            }
        }
        return false;
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

    public static Comparator<City> com = new Comparator<City>(){//for sotieren
        @Override
        public int compare(City o1, City o2) {
            if(o1.distance > o2.distance){
                return 1;
            }
            else if(o1.distance == o2.distance){
                return 0;
            }
            else{
                return -1;
            }
        
        }
    };

    @Override
    protected Object clone(){
        City c = new City(this.x, this.y);
        c.distance = this.distance;
        c.parent = (City)this.parent.clone();
        c.isRoad = this.isRoad;
        c.state = this.state;
        return c;
    }
}

class Edge{ //Xinchuan Wang
    City from;
    City to;
    double w = 1;

    public Edge(City from, City to){
        this.from = from;
        this.to = to;
    }

    public City getOther(City c){
        if(from.equals(c)){
            return to;
        }
        else if(to.equals(c)){
            return from;
        }

        return null;
    }
}

class Road{
    int startX;
    int startY;
    int endX;
    int endY;
    int speed = 2;

    public Road(int startX, int startY, int endX, int endY){
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }

    public Road(int startX, int startY, int endX, int endY, int speed){
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.speed = speed;
    }
}

public class VD2 extends JFrame{//Xinyu Tu
    private int width = 1250;
    private int height = 850;
    static  int gridWidth = 15;
    static  int gridheight = 15;
    private int fontSize = 15;
    private int colNumber = 20; 
    private int rowNumber = 20; 
    private int k = 1;
    private int cityCounter = 20;
    private int speed = 2;

    private double maxDistance = 5.0;

    private City[][] cities = new City[colNumber][rowNumber];
    private ArrayList<Road> roads = new ArrayList<Road>();
    private ArrayList<Road> newRoads = new ArrayList<Road>();
    private ArrayList<Edge> edges = new ArrayList<Edge>();

    private Map<City, Map<Double, ArrayList<City>>> dij = new HashMap<City, Map<Double, ArrayList<City>>>();
    private final Random rnd = new Random();
    
    ArrayList<City> allSort = new ArrayList<City>();

    HashMap<HashSet<City>, ArrayList<City> > topCities = new HashMap<HashSet<City>, ArrayList<City>>();//put top k cities
    
    public VD2(){
        this.setTitle("VD");
        this.setSize(width, height);
        this.setLocation(20, 15);
        this.setDefaultCloseOperation(3);
        
        init();

        this.setLayout(null);

        Label label1 = new Label("K:");
        label1.setBounds(800, 100, 50, 20);
        this.add(label1);

        TextField ktext = new TextField();
        ktext.setText(String.valueOf(k));
        ktext.setBounds(850, 100, 150, 20);
        ktext.addKeyListener(new KeyListener(){
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == '\n'){//enter
                    k = Integer.parseInt(ktext.getText());
                    allDistance();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {}
        });
        this.add(ktext);
        
        Label label2 = new Label("v:");
        label2.setBounds(800, 130, 50, 20);
        this.add(label2);

        TextField sppedtext = new TextField();
        sppedtext.setText(String.valueOf(speed));
        sppedtext.setBounds(850, 130, 150, 20);
        sppedtext.addKeyListener(new KeyListener(){
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == '\n'){
                    speed = Integer.parseInt(sppedtext.getText());
                    for(Edge ed : edges){
                        ed.w = 1.0 / speed;
                    }
                    allDistance();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {}
        });
        this.add(sppedtext);

        Label label3 = new Label("Zeile:");
        label3.setBounds(800, 160, 50, 20);
        this.add(label3);

        TextField rowtext = new TextField();
        TextField columntext = new TextField();
        rowtext.setText(String.valueOf(rowNumber));
        rowtext.setBounds(850, 160, 150, 20);
        rowtext.addKeyListener(new KeyListener(){
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == '\n'){
                    colNumber = Integer.parseInt(columntext.getText());
                    rowNumber = Integer.parseInt(rowtext.getText());
                    init();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {}
        });
        this.add(rowtext);

        Label label4 = new Label("Spalte:");
        label4.setBounds(800, 190, 50, 20);
        this.add(label4);
        
        columntext.setText(String.valueOf(colNumber));
        columntext.setBounds(850, 190, 150, 20);
        columntext.addKeyListener(new KeyListener(){
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == '\n'){
                    colNumber = Integer.parseInt(columntext.getText());
                    rowNumber = Integer.parseInt(rowtext.getText());
                    init();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {}
        });
        this.add(columntext);
        
        Label label5 = new Label("Highway:");
        label5.setBounds(800, 220, 50, 20);
        this.add(label5);

        TextField roadtext = new TextField();
        roadtext.setBounds(850, 220, 150, 20);
        roadtext.addKeyListener(new KeyListener(){
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == '\n'){
                    String[] tmp = roadtext.getText().split(",");
                    if(tmp.length == 4){
                        newRoads.add(new Road(
                            Integer.parseInt(tmp[0]),
                            Integer.parseInt(tmp[1]),
                            Integer.parseInt(tmp[2]),
                            Integer.parseInt(tmp[3])
                        ));
                        init();
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {}
        });
        this.add(roadtext);

        this.addMouseMotionListener(new MouseMotionListener(){
            @Override
            public void mouseDragged(MouseEvent e) {}

            @Override
            public void mouseMoved(MouseEvent e) {}
            
        });

        this.addMouseListener(new MouseListener(){
            int pointX = -1;
            int pointY = -1;
            @Override
            public void mouseClicked(MouseEvent e) {
                int oriX = e.getX() - gridWidth;
                int oriY = e.getY() - gridheight;
                int x = oriX / gridWidth;
                int y = oriY / gridheight;
                if(oriX % gridWidth >= City.redis){
                    x++;
                }
                if(oriY % gridheight >= City.redis){
                    y++;
                }

                if(e.getButton() == 1){//add city
                    if(pointX != -1 && pointY != -1){
                        cities[pointX][pointY].state = 0;
                        pointX = -1;
                        pointY = -1;
                        allSort.clear();
                    }

                    cities[x][y].state = 1;
                    cities[x][y].name = String.valueOf(cityCounter);
                    cityCounter++;

                    //dijkstra(cities[x][y]);
                    //contourLine(cities[x][y]);

                    allDistance();
                }
                else if(e.getButton() == 3 && cities[x][y].state == 0){
                    if(pointX != -1 && pointY != -1){
                        cities[pointX][pointY].state = 0;
                    }
                    cities[x][y].state = 2;
                    dijkstra(cities[x][y]);
                    sortCities();
                    pointX = x;
                    pointY = y;
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}
        });
    }

    public void init(){//Xinchuan Wang  
        gridWidth = (height - 70) / colNumber;
        gridheight = (height - 70) / rowNumber;
        System.out.println(colNumber);
        System.out.println(rowNumber);
        System.out.println(gridWidth);
        System.out.println(gridheight);
        City.redis = (int)(gridWidth * 0.7);

        cities = new City[colNumber][rowNumber];
        edges.clear();
        roads.clear();

        roads.add(new Road(1, 2, 1, 8));
        roads.add(new Road(6, 10, 6, 11));
        roads.add(new Road(13, 12, 15, 12));
        roads.add(new Road(11, 2, 15, 2));
        roads.add(new Road(10, 4, 10, 12));
        roads.add(new Road(4, 6, 12, 6));
        roads.addAll(newRoads);

        for(int i = 0;i<colNumber;i++){
            for(int j = 0;j<rowNumber;j++){
                City c = new City(i, j);
                cities[i][j] = c;
            }
        }
        
        addCity(2, 2, "0");
        addCity(10, 6, "1");
        addCity(6, 13, "2");
        addCity(14, 8, "3");
        addCity(12, 11, "4");
        addCity(4, 6, "5");
        addCity(13, 3, "6");
        addCity(11, 9, "7");
        addCity(9, 15, "8");
        addCity(9, 5, "9");
        addCity(10, 12, "10");
        addCity(12, 15, "11");
        addCity(15, 5, "12");
        addCity(5, 13, "13");
        addCity(13, 13, "14");
        addCity(12, 3, "15");
        addCity(7, 6, "16");
        addCity(8, 11, "17");
        addCity(3, 5, "18");
        addCity(11, 8, "19");

        for (Road r : roads) {
            for(int x = r.startX;x<=r.endX;x++){
                for(int y = r.startY;y<=r.endY;y++){
                    cities[x][y].isRoad = true;
                }
            }
        }

        for(int i = 0;i<colNumber;i++){
            for(int j = 0;j<rowNumber;j++){
                Edge ed = null;
                if(i != 0){
                    ed = new Edge(cities[i][j], cities[i-1][j]);
                    cities[i][j].edges.add(ed);
                    edges.add(ed);

                    if(ed != null){
                        if(ed.from.isRoad && ed.to.isRoad){
                            ed.w = 1.0 / speed;
                        }
                    }
                }
                if(i != colNumber-1){
                    ed = new Edge(cities[i][j], cities[i+1][j]);
                    cities[i][j].edges.add(ed);
                    edges.add(ed);

                    if(ed != null){
                        if(ed.from.isRoad && ed.to.isRoad){
                            ed.w = 1.0 / speed;
                        }
                    }
                }
                    
                if(j != 0){
                    ed = new Edge(cities[i][j], cities[i][j-1]);
                    cities[i][j].edges.add(ed);
                    edges.add(ed);

                    if(ed != null){
                        if(ed.from.isRoad && ed.to.isRoad){
                            ed.w = 1.0 / speed;
                        }
                    }
                }
                    
                if(j != rowNumber - 1){
                    ed = new Edge(cities[i][j], cities[i][j+1]);
                    cities[i][j].edges.add(ed);
                    edges.add(ed);

                    if(ed != null){
                        if(ed.from.isRoad && ed.to.isRoad){
                            ed.w = 1.0 / speed;
                        }
                    }
                }
            }
        }

        allDistance();//color
    }

    public void paint(Graphics g) {//Xinyu Tu
        super.repaint();

        Image image = createImage(width, height);
        Graphics graphics = image.getGraphics();

        graphics.drawRect(0, 0, 1300, 1300);//draw Gitter
        graphics.setColor(Color.GRAY);

        for (Edge ed : edges) {
            graphics.drawLine(ed.from.centerX(),ed.from.centerY(),ed.to.centerX(), ed.to.centerY());

            if(ed.from.centerX() == ed.to.centerX()){
                //graphics.drawString(String.valueOf(ed.w), ed.from.centerX(), 
                //    ed.from.centerY() + (ed.to.centerY() - ed.from.centerY()) / 2);
            }

            if(ed.from.centerY() == ed.to.centerY()){
                //graphics.drawString(String.valueOf(ed.w), 
                //    ed.from.centerX() + (ed.to.centerX() - ed.from.centerX()) / 2, 
                //    ed.from.centerY());
            }
        }
        
        for(int i = 0;i<colNumber;i++){
            for(int j = 0;j<rowNumber;j++){
                if(cities[i][j].state == 1){
                    graphics.setColor(cities[i][j].color);
                    graphics.fillOval(
                        cities[i][j].realX(City.redis), cities[i][j].realY(City.redis), 
                        City.redis, City.redis);
                    graphics.setColor(Color.BLACK);
                    graphics.setFont(new Font("Arial",Font.BOLD,fontSize));
                    graphics.drawString(String.valueOf(cities[i][j].name), 
                        cities[i][j].centerX() - 12, cities[i][j].centerY() -10);
                }
//                else if(cities[i][j].state == 2){
//                    graphics.setColor(Color.YELLOW);
//                    graphics.fillOval(
//                        cities[i][j].realX(City.redis), cities[i][j].realY(City.redis), 
//                        City.redis, City.redis);
//                }
                
                /*
                if(cities[i][j].distance != Integer.MAX_VALUE){
                    graphics.setColor(Color.BLACK);
                    graphics.setFont(new Font("Times New Roman",Font.PLAIN,10));
                    graphics.drawString(String.valueOf(cities[i][j].distance), 
                        cities[i][j].centerX(), cities[i][j].centerY());
                }
                */
            }
        }

        for (Road r : roads) {
            City start = new City(r.startX, r.startY);
            City end = new City(r.endX, r.endY);
            graphics.setColor(Color.BLUE);
            
            if(start.centerX() == end.centerX()){
                graphics.fillRect(
                    start.centerX() - 2,
                    start.centerY() , 
                    4,
                    Math.abs(start.centerY() - end.centerY()) 
                );
            }
            else{
                graphics.fillRect(
                    start.centerX(),
                    start.centerY() - 2, 
                    Math.abs(end.centerX() - start.centerX()),
                    end.centerY() - end.centerY() + 4
                );
            }
            
            //graphics.drawLine(start.centerX(),start.centerY(), end.centerX(), end.centerY());
        }
//
//        for(City key : dij.keySet()){
//            graphics.setColor(key.color);
//            Map<Double, ArrayList<City>> map = dij.get(key);
//            for(double k = 0.5;k<=maxDistance;k+=0.5){
//                if(map.containsKey(k)){
//                    ArrayList<City> all = map.get(k);
//                    City start = null;
//                    City end = null;
//                    for(City city : all){
//                        if(start == null){
//                            start = city;
//                        }
//                        else if(end == null){
//                            end = city;
//                            graphics.drawLine(start.centerX(),start.centerY(), end.centerX(), end.centerY());
//                        }
//                        else{
//                            start = end;
//                            end = city;
//                            graphics.drawLine(start.centerX(),start.centerY(), end.centerX(), end.centerY());
//                        }
//                    }
//    
//                    if(all.size() > 2){
//                        start = all.get(0);
//                        graphics.drawLine(start.centerX(),start.centerY(), end.centerX(), end.centerY());
//                    }
//                }
//            }
//        }

        for(HashSet<City> key : topCities.keySet()){
            ArrayList<City> points = topCities.get(key);
            for(City c : points){
                graphics.setColor(c.color);
                graphics.fillOval(
                    c.realX(City.redis / 2), c.realY(City.redis / 2), 
                    City.redis / 2, City.redis / 2);
            }
        }

//        for(int i =0;i<allSort.size();i++){
//            City ct = allSort.get(i);
//            graphics.setColor(Color.BLACK);
//            graphics.setFont(new Font("Times New Roman",Font.PLAIN,20));
//            graphics.drawString(ct.name + " : " + String.valueOf(ct.distance), 1100, 100 + 30 * i);
//        }

        g.drawImage(image, 0, 0, null);
    }

    public static double getDistance(int x1, int y1, int x2, int y2){//L-1 Metric
    	 return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    public void dijkstra(City start){
        for(int i = 0;i<colNumber;i++){
            for(int j = 0;j<rowNumber;j++){
                cities[i][j].distance = Integer.MAX_VALUE;
                cities[i][j].parent = null;
            }
        }
        start.distance = 0;
        
        PriorityQueue<City> Q = new PriorityQueue<City>(City.com);
        for(int i = 0;i<colNumber;i++){
            for(int j = 0;j<rowNumber;j++){
                Q.add(cities[i][j]);
            }
        }

        while(Q.isEmpty() == false){
            City u = Q.poll(); 

            for (Edge ed : u.edges) {
                City v = ed.getOther(u);
                if(v.distance > u.distance + ed.w){
                    Q.remove(v);
                    v.distance = u.distance + ed.w;
                    v.parent = u;
                    Q.add(v);
                }
            }
        }
    }

//    private void contourLine(City start){//Xinchuan Wang
//        HashMap<Double, ArrayList<City>> map = new HashMap<Double, ArrayList<City>>();
//        dij.put(start, map);
//        int y = rnd.nextInt(16,235);
//        int u = rnd.nextInt(16,240);
//        int v = rnd.nextInt(16,240);
//
//        start.color = new Color(y,u,v);
//        for(double m = 0.5;m<=maxDistance;m+=0.5){
//            map.put(m, new ArrayList<City>());
//            for(int i = 0;i<colNumber;i++){
//                for(int j = 0;j<rowNumber;j++){
//                    if(cities[i][j].distance == m){
//                        map.get(m).add(cities[i][j]);
//                    }
//                }
//            }
//        }
//
//        for(double m = 0.5;m<=maxDistance;m+=0.5){
//            if(map.containsKey(m)){
//                ArrayList<City> result = new ArrayList<>();
//                ArrayList<Double> angle = new ArrayList<>();
//                for (City city : map.get(m)) {
//                    angle.add(Math.atan2(city.y - start.y, city.x - start.x));
//                }
//                
//                List<Integer> indices = IntStream.range(0, angle.size()).boxed().sorted(Comparator.comparing(angle::get))
//                            .collect(Collectors.toList());
//    
//                for (int n = 0; n < indices.size(); n++) {
//                    result.add(map.get(m).get(indices.get(n)));
//                }
//                map.put(m, result);
//            }
//        }
//    }

    private void sortCities(){
        allSort.clear();
        for(int i = 0;i<colNumber;i++){
            for(int j = 0;j<rowNumber;j++){
                if(cities[i][j].state == 1){
                    allSort.add(cities[i][j]);
                }
            }
        }
        allSort.sort(City.com);
    }

    private void addCity(int x, int y, String name ){
        cities[x][y].state = 1;
        cities[x][y].name = String.valueOf(name);
    }

    private Road getRoad(Edge ed){
        for(Road r : roads){
            if(r.startX <= ed.from.x && r.startY == ed.from.y && 
                r.endX >= ed.to.x && r.endY == ed.to.y){
                    return r;
            }
            if(r.startX == ed.from.x && r.startY <= ed.from.y && 
                r.endX == ed.to.x && r.endY >= ed.to.y){
                    return r;
            }
        }

        return null;
    }

    private void allDistance(){//Xinyu Tu
        topCities.clear();//hashmap clear
        int colorIndex = 0;
        for(int i = 0;i<colNumber;i++){
            for(int j = 0;j<rowNumber;j++){
                if(cities[i][j].state == 0){//city==1，point==0

                    allSort.clear();
                    dijkstra(cities[i][j]);//O(V(log(V)+E))
                    
                    sortCities();//Quicksort,Anzahl der Stadt:c->O(c*log(c))
                    
                    
                    HashSet<City> top = new HashSet<City>();
                    for(int p = 0;p<k;p++){//O(k）,k-mal iteration
                        City c = allSort.get(p);
                        top.add(c);
                    }

                    if(topCities.containsKey(top)){
                        cities[i][j].color = topCities.get(top).get(0).color;
                        topCities.get(top).add(cities[i][j]);//O(1)
                    }
                    else{
                        
                        cities[i][j].color = colorList.get(colorIndex);
                        colorIndex++;
                        ArrayList<City> points = new ArrayList<City>();
                        points.add(cities[i][j]);
                        topCities.put(top, points);//O(1)
                    }//insgesamt O(V(V(log(V)+E))+(c*log(c))+k))
                }
            }
        }

        allSort.clear();
    }

    private final List<Color> colorList = List.of(
        new Color(1,0,103), 
        new Color(213,255,0), 
        new Color(255,0,86), 
        new Color(158,0,142), 
        new Color(14,76,161), 
        new Color(255,229,2), 
        new Color(0,95,57), 
        new Color(0,255,0), 
        new Color(149,0,58), 
        new Color(255,147,126),
        new Color(164,36,0), 
        new Color(0,21,68), 
        new Color(145,208,203), 
        new Color(98,14,0), 
        new Color(107,104,130), 
        new Color(0,0,255), 
        new Color(0,125,181), 
        new Color(106,130,108), 
        new Color(0,174,126), 
        new Color(194,140,159), 
        new Color(190,153,112), 
        new Color(0,143,156), 
        new Color(95,173,78), 
        new Color(255,0,0), 
        new Color(255,0,246), 
        new Color(255,2,157), 
        new Color(104,61,59), 
        new Color(255,116,163), 
        new Color(150,138,232), 
        new Color(152,255,82), 
        new Color(167,87,64), 
        new Color(1,255,254), 
        new Color(255,238,232), 
        new Color(254,137,0), 
        new Color(189,198,255), 
        new Color(1,208,255), 
        new Color(187,136,0), 
        new Color(117,68,177), 
        new Color(165,255,210), 
        new Color(255,166,254), 
        new Color(119,77,0), 
        new Color(122,71,130), 
        new Color(38,52,0), 
        new Color(0,71,84), 
        new Color(67,0,44), 
        new Color(181,0,255), 
        new Color(255,177,103), 
        new Color(255,219,102), 
        new Color(144,251,146), 
        new Color(126,45,210), 
        new Color(189,211,147), 
        new Color(229,111,254), 
        new Color(222,255,116), 
        new Color(0,255,120), 
        new Color(0,155,255), 
        new Color(0,100,1), 
        new Color(0,118,255), 
        new Color(133,169,0), 
        new Color(0,185,23), 
        new Color(120,130,49), 
        new Color(0,255,198), 
        new Color(255,110,65), 
        new Color(232,94,190)
    );

    public static void main(String[] args) {
        VD2 vd = new VD2();
        vd.setVisible(true);
    }
}

