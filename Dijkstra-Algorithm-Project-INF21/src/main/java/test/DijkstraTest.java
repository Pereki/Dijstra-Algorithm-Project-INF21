package test;

import model.Edge;
import model.Graph;
import model.Vertex;
import service.Dijkstra;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

import static model.Controller.loadGraph;


public class DijkstraTest {
    public static void main(String[] args) throws Exception {
        Vertex B = new Vertex(1,48.76976,9.17399);  //Berlin
        Vertex S = new Vertex(2,52.52204,13.41648); //Stuttgart
        Vertex L = new Vertex(3,51.34556,12.28058); //Leipzig
        Vertex N = new Vertex(4,49.45598,11.08208); //Nürnberg
        Vertex M = new Vertex(5,48.13899,11.56157); //München
        Vertex F = new Vertex(6,50.11145,8.66331);  //Frankfurt a.M.
        Vertex D = new Vertex(7,51.50876,7.50876);  //Dortmund
        Vertex H = new Vertex(8,52.36271,9.72252);  //Hannover
        Vertex HH = new Vertex(9,53.54767,10.00187); //Hamburg

        ArrayList vertexes = new ArrayList(Arrays.asList(new Vertex[]{B,S,L,N,M,F,D,H,HH}));

        Edge e1 = new Edge(B,L);
        Edge e2 = new Edge(B,H);
        Edge e3 = new Edge(B,HH);
        Edge e4 = new Edge(L,N);
        Edge e5 = new Edge(N,S);
        Edge e6 = new Edge(N,M);
        Edge e7 = new Edge(S,M);
        Edge e8 = new Edge(S,F);
        Edge e9 = new Edge(F,N);
        Edge e10 = new Edge(F,L);
        Edge e11 = new Edge(F,D);
        Edge e12 = new Edge(D,H);
        Edge e13 = new Edge(D,L);
        ArrayList edges = new ArrayList(Arrays.asList(new Edge[]{e1,e2,e3,e4,e5,e6,e7,e8,e9,e10,e11,e12,e13}));

        //Graph raw = new Graph(edges,vertexes);
        //saveGraph(raw,"graph.graph");
        //Graph raw = loadGraph("graph.graph");
        //Graph g = Dijkstra.getShortWay(raw,B,F);

        //for(Vertex v:g.getVertexList()){
            //System.out.print(v.getId());
        //}
        //System.out.println();
        //double length =0;
        //for(Edge e: g.getEdgeList()){
            //length+=e.getLength();
        //}
        //System.out.println(length);

        System.out.println("finish");


        XmlParserTest parser = new XmlParserTest();

        if(parser.crossingTest()){
            System.out.println("Parser läuft");
        }

        parser.graphTest();

    }



}
