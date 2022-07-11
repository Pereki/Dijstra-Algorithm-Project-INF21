package controller;

import com.almasb.fxgl.core.collection.PropertyChangeListener;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import model.*;
import service.Dijkstra;
import service.XmlParser;
import view.GraphRenderer;
import view.GraphRendererOptions;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.Timestamp;
import java.util.*;

public class Controller implements Initializable {

    private static final double MAX_ZOOM_LEVEL = 2.5;
    private static final double MIN_ZOOM_LEVEL = 0.25;
    private static final double ZOOM_IN_MULTIPLIER = 1.25;
    private static final double ZOOM_OUT_MULTIPLIER = 0.8;
    private static final String ROADS_KEY = "ROADS";
    private static final GraphRendererOptions ROADS_OPTIONS = new GraphRendererOptions()
            .routeColor(Color.valueOf("#154889"))
            .dotJunctions(false)
            .showLabels(false);
    private static final String BORDERS_KEY = "BORDERS";
    private static final GraphRendererOptions BORDERS_OPTIONS = new GraphRendererOptions()
            .routeColor(Color.valueOf("#bfbfbf"))
            .dotJunctions(false)
            .showLabels(false);
    private static final String ROUTE_KEY = "ROUTE";
    private static final GraphRendererOptions ROUTE_OPTIONS = new GraphRendererOptions()
            .routeColor(Color.RED)
            .dotJunctions(false)
            .showLabels(true);

    @FXML
    private ScrollPane scrollpane;
    @FXML
    private Group groupGraphs;
    @FXML
    private ComboBox<String> inputStart;
    @FXML
    private ComboBox<String> inputDestination;

    private HashMap<String, Vertex> junctions = new HashMap<>();

    private GraphRenderer renderer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // for Germany: W: 5.866342; E: 15.041892; N: 55.058307; S: 47.270112;
        this.renderer = new GraphRenderer(groupGraphs, new GeoBounds(
                5.866342, 15.041892, 55.058307, 47.270112
        ));
    }

    @FXML
    protected void onDrawButtonClick() {
        if (junctions == null) {
            showError("Es muss zuerst ein Straßennetz geladen werden.");
            return;
        }

        Vertex start = junctions.get(inputStart.getValue());
        Vertex dest = junctions.get(inputDestination.getValue());

        if (start == null) {
            showError("Der angegebene Startort existiert im geladenen Straßennetz nicht.");
            return;
        }
        if (dest == null) {
            showError("Der angegebene Zielort existiert im geladenen Straßennetz nicht.");
            return;
        }

        Platform.runLater(() -> {
            Graph route;
            try {
                route = Dijkstra.getShortWay(getRoadsGraph(), start, dest);
            } catch (Exception e) {
                showError(String.format("Es konnte keine Route von %s nach %s berechnet werden.", start.getIdentifier(), dest.getIdentifier()));
                return;
            }
            setRouteGraph(route);
        });
    }

    @FXML
    protected void onInputStartKeyEvent(KeyEvent e) {
        displaySuggestions(inputStart);
    }

    @FXML
    protected void onInputDestinationKeyEvent(KeyEvent e) {
        displaySuggestions(inputDestination);
    }

    private void displaySuggestions(ComboBox input) {
        String text = input.getEditor().getText();
        ObservableList<String> matches = FXCollections.observableList(findMatches(text));
        input.setItems(matches);
        if (matches.size() > 0) {
            input.show();
        } else {
            input.hide();
        }
    }

    private List<String> findMatches(String text) {
        List<String> matches = new ArrayList<>();
        for (String key : junctions.keySet()) {
            if (key.toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT))) {
                matches.add(key);
            }
        }
        return matches;
    }

    @FXML
    protected void onButtonZoomOutClick() {
        double scale = scrollpane.getContent().getScaleX();
        scale *= ZOOM_OUT_MULTIPLIER;
        if (scale < MIN_ZOOM_LEVEL) return;
        setZoomFactor(scale);
    }

    @FXML
    protected void onButtonZoomInClick() {
        double scale = scrollpane.getContent().getScaleX();
        scale *= ZOOM_IN_MULTIPLIER;
        if (scale > MAX_ZOOM_LEVEL) return;
        setZoomFactor(scale);
    }

    protected void setZoomFactor(double factor) {
        scrollpane.getContent().setScaleX(factor);
        scrollpane.getContent().setScaleY(factor);
        scrollpane.getContent().setScaleY(factor);
    }

    @FXML
    protected void onMenuButtonContributorsClick() {
        String text = "Lukas Burkhardt (Algorithmus) # Pascal Fuchs (Datenstruktur) # Ruben Kraft (API) # Paul Lehmann (SVG Parser) # David Maier (GUI)";
        Alert alert = new Alert(Alert.AlertType.NONE, text.replaceAll(" # ", "\n"), ButtonType.CLOSE);
        alert.setTitle("Mitwirkende");
        alert.showAndWait();
    }

    @FXML
    protected void onMenuButtonMapExportClick() {
        Graph graph = getBordersGraph();
        if (graph == null) {
            showError("Es ist keine Karte geladen.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Karte speichern unter");
        File file = fileChooser.showSaveDialog(scrollpane.getScene().getWindow());

        Platform.runLater(() -> {
            try {
                SerializeService.saveGraph(graph, file.getAbsolutePath());
                setBordersGraph(graph);
            } catch (IOException e) {
                showError("Die Karte konnte nicht exportiert werden.");
                e.printStackTrace();
                return;
            }
        });
    }

    @FXML
    protected void onMenuButtonMapImportClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Karte öffnen");
        File file = fileChooser.showOpenDialog(scrollpane.getScene().getWindow());

        Platform.runLater(() -> {
            try {
                Graph graph = SerializeService.loadGraph(file.getAbsolutePath());
                setBordersGraph(graph);
            } catch (IOException | ClassNotFoundException e) {
                showError("Die angegebene Datei konnte nicht als Karte geladen werden.");
                e.printStackTrace();
            }
        });
    }

    @FXML
    protected void onMenuButtonRouteExportClick() {
        Graph graph = getBordersGraph();
        if (graph == null) {
            showError("Es ist kein Straßennetz geladen.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Straßennetz speichern unter");
        File file = fileChooser.showSaveDialog(scrollpane.getScene().getWindow());

        Platform.runLater(() -> {
            try {
                SerializeService.saveGraph(graph, file.getAbsolutePath());
                setBordersGraph(graph);
            } catch (IOException e) {
                showError("Das Straßennetz konnte nicht exportiert werden.");
                e.printStackTrace();
                return;
            }
        });
    }

    @FXML
    protected void onMenuButtonRouteImportClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Straßennetz öffnen");
        File file = fileChooser.showOpenDialog(scrollpane.getScene().getWindow());

        Platform.runLater(() -> {
            try {
                Graph graph = SerializeService.loadGraph(file.getAbsolutePath());
                setRoadsGraph(graph);
            } catch (IOException | ClassNotFoundException e) {
                showError("Die angegebene Datei konnte nicht als Straßennetz geladen werden.");
                e.printStackTrace();
            }
        });
    }

    @FXML
    protected void onMenuButtonOsmRoadsImportClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("OSM-Datei mit Straßennetz öffnen");
        File file = fileChooser.showOpenDialog(scrollpane.getScene().getWindow());

        Platform.runLater(() -> {
            XmlParser p = new XmlParser(file.getAbsolutePath());
            Graph g = p.getGraph();
            setRoadsGraph(g);
        });
    }

    @FXML
    protected void onMenuButtonOsmBordersImportClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("OSM-Datei mit Ländergrenzen öffnen");
        File file = fileChooser.showOpenDialog(scrollpane.getScene().getWindow());

        Platform.runLater(() -> {
            XmlParser p = new XmlParser(file.getAbsolutePath());
            Graph g = p.getGraph();
            setBordersGraph(g);
        });
    }

    protected void setBordersGraph(Graph graph) {
        Platform.runLater(() -> {
            renderer.removeGraphLayer(BORDERS_KEY);
            renderer.addGraphLayer(BORDERS_KEY, graph, BORDERS_OPTIONS);
        });
    }

    protected Graph getBordersGraph() {
        return renderer.getGraphLayer(BORDERS_KEY);
    }

    protected void setRoadsGraph(Graph graph) {
        Platform.runLater(() -> {
            setJunctions(graph.getVertexList());
            renderer.removeGraphLayer(ROADS_KEY);
            renderer.addGraphLayer(ROADS_KEY, graph, ROADS_OPTIONS);
        });
    }

    private void setJunctions(List<Vertex> vertices) {
        this.junctions = new HashMap<>();
        GeoBounds b = new GeoBounds(Double.MAX_VALUE, Double.MIN_VALUE, Double.MIN_VALUE, Double.MAX_VALUE);
        for (Vertex v : vertices) {

            // expand viewport if necessary
//            if (v.getLat() < b.getNorth()) b.setNorth(v.getLat());
//            if (v.getLat() > b.getSouth()) b.setSouth(v.getLat());
//            if (v.getLon() < b.getWest()) b.setWest(v.getLon());
//            if (v.getLon() > b.getEast()) b.setEast(v.getLon());

            if (v.getJunction()) {
                junctions.put(v.getIdentifier(), v);
                inputStart.getItems().add(v.getIdentifier());
                inputDestination.getItems().add(v.getIdentifier());
            }
        }
    }

    protected Graph getRoadsGraph() {
        return renderer.getGraphLayer(ROADS_KEY);
    }

    protected void setRouteGraph(Graph graph) {
        Platform.runLater(() -> {
            renderer.removeGraphLayer(ROUTE_KEY);
            renderer.addGraphLayer(ROUTE_KEY, graph, ROUTE_OPTIONS);
        });
    }

    protected Graph getRouteGraph() {
        return renderer.getGraphLayer(ROUTE_KEY);
    }

    private void showError(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR, text, ButtonType.CLOSE);
        alert.setTitle("Fehler");
        alert.showAndWait();
    }
}