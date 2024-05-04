package cs1302.api;

// javafx
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.text.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.event.*;
import javafx.scene.layout.*;
import javafx.collections.*;

// javanet
import java.net.http.*;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.URL;
import java.net.URI;
import java.net.URLEncoder;

// gson
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

// extra
import java.io.InputStream;
import java.lang.Exception;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.Base64;

/**
 * This app implements two api's, Edamam Recipes and OpenFoodFacts.
 * It will showcase the top 20 recipes from the requested search word
 * then it will show a list of related products.
 */
public class ApiApp extends Application {
    Stage stage;
    Scene scene;
    BorderPane root;
    ListView<Label> recipeList;
    ListView<String> productList;
    List<Label> list;
    ArrayList<String> products;

    HBox searchLayer;
    HBox instructionLayer;
    HBox productLayer;
    HBox mainLayer;
    VBox sideLayer;
    VBox topLayer;

    TextField searchBox;
    TextField productBox;
    Text search;
    Text product;
    Text instruction;
    Text recipeName;
    Button getRecipes;
    Button getProducts;
    Image img;
    ImageView imgView;
    String accessToken;

    final String instructionDef = "Type in a recipe and press get recipes and get products.";
    final String recipeKey = "41191a205a196f9830c5d43ffd55a9d8";
    final String recipeId = "35b6401b";
    final String recipeURL = "https://api.edamam.com/api/recipes/v2/";
    final String productURL = "https://world.openfoodfacts.net/api/v2/search";

    // HTTP client
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    // Gson object
    public static Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .create();

    /**
     * Constructs an {@code ApiApp} object. This default (i.e., no argument)
     * constructor is executed in Step 2 of the JavaFX Application Life-Cycle.
     */
    public ApiApp() {
        root = new BorderPane();
        stage = null;
        scene = null;
        productList = new ListView<>();
        recipeList = new ListView<>();

        searchLayer = new HBox();
        instructionLayer = new HBox();
        productLayer = new HBox();
        mainLayer = new HBox();
        sideLayer = new VBox();
        topLayer = new VBox();

        searchBox = new TextField();
        search = new Text("Search:");
        recipeName = new Text();
        instruction = new Text(instructionDef);
        getRecipes = new Button("Get Recipes");
        getProducts = new Button("Get Products");
        img = new Image("https://cdn-icons-png.flaticon.com/512/259/259164.png");
        imgView = new ImageView(img);
        imgView.setFitWidth(100);
        imgView.setFitHeight(100);

        search.setFont(new Font(13));

        ObservableList<Label> recipeLinks = FXCollections.observableArrayList();
    } // ApiApp

    /** {@inheritDoc} */
    @Override
    public void init() {
        HBox.setHgrow(searchBox, Priority.ALWAYS);
        HBox.setHgrow(recipeList, Priority.ALWAYS);
        HBox.setHgrow(productList, Priority.ALWAYS);

        // search layer
        searchLayer.getChildren().addAll(search, searchBox, getRecipes);

        // instruction layer
        instructionLayer.getChildren().add(instruction);

        // product layer
        productLayer.getChildren().addAll(getProducts);

        // main, side, & top layer
        mainLayer.getChildren().add(recipeList);
        sideLayer.getChildren().add(productList);
        topLayer.getChildren().addAll(searchLayer, productLayer);

        // border pane
        root.setTop(topLayer);
        root.setBottom(instructionLayer);
        root.setLeft(mainLayer);
        root.setRight(sideLayer);
//        root.setLeft(imgView);
//        root.setPrefSize(500,500);

        // get recipes thread
        getRecipes.setOnAction(e -> {
            Thread recipeThread = new Thread(() -> loadRecipes());
            recipeThread.setDaemon(true);
            recipeThread.start();
        });

        // get products thread
        getProducts.setOnAction(e -> {
            Thread storeThread = new Thread(() -> loadProducts());
            storeThread.setDaemon(true);
            storeThread.start();
        });

    } // init

    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {

        this.stage = stage;

        // setup scene
        scene = new Scene(root);

        // setup stage
        stage.setTitle("ApiApp!");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> Platform.exit());
        stage.sizeToScene();
        stage.show();

    } // start

    /**
     * This method will load the recipes in list form.
     * It will show results from the recipe searched and a url link for the user
     * to click on and go to the website.
     */
    public void loadRecipes() {
        try {
            List<Label> list2 = recipeSearch();
            if (list2.size() < 1) {
                String error1 = "error";
            } else {
                Platform.runLater(() -> recipeList.getItems().clear());
                Platform.runLater(() -> recipeList.getItems().addAll(list2));
            } // if-else
            list = list2;
            Platform.runLater(() -> instruction.setText("Loading Recipe list, click on a recipe" +
                " to open the webpage."));
        } catch (Exception e) {
            Platform.runLater(() -> alertError(e, requestRecipeLink()));
        } // try-catch

    } // loadRecipes

    /**
     * This method is used to get the top result for a recipe.
     * @return an array list
     * @throw IOExcpetion -- status is not 200
     * @throw IllegalArgumentException -- num of results is not 1 or more
     */
    public List<Label> recipeSearch() {
        List<Label> ingredientList = new ArrayList<>();
        try {
            String searchItem = requestRecipeLink();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(searchItem)).build();
            HttpResponse response = HTTP_CLIENT.send(request, BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                Platform.runLater(() -> instruction.setText("Finding recipe failed, try again."));
                throw new IOException("Finding recipe failed. try again.");
            } // if
            String json = response.body().toString();
            RecipeResponse recipeResponse = GSON.fromJson(json, RecipeResponse.class);
            if (recipeResponse.count < 1) {
                Platform.runLater(() -> instruction.setText("No recipe found, try again."));
                throw new IllegalArgumentException("No recipe found, try again.");
            } // if
            if (ingredientList != null) {
                ingredientList.clear();
            } // if

            for (int i = 0; i < recipeResponse.hits.length; i++) {
                RecipeResult recipeResult = recipeResponse.hits[i];
                Label label = createHyperlinkLabel(recipeResult.recipe.label,
                    recipeResult.recipe.url);
                ingredientList.add(label);
            } // for
            return ingredientList;
        } catch (Exception e) {
            Platform.runLater(() -> alertError(e, requestRecipeLink()));
            return ingredientList;
        } // try-catch
    } // recipeSearch

    /**
     * This method will return a url for the recipe search.
     * @return a string
     */
    public String requestRecipeLink() {
        String searchText = searchBox.getText();

        String type = URLEncoder.encode("public", StandardCharsets.UTF_8);
        String q = URLEncoder.encode(searchText, StandardCharsets.UTF_8);
        String appId = URLEncoder.encode(recipeId, StandardCharsets.UTF_8);
        String appKey = URLEncoder.encode(recipeKey, StandardCharsets.UTF_8);

        String query = "?type=" + type + "&q=" + q + "&app_id=" + appId + "&app_key=%20" + appKey
            + "%09";
        String requestLink = recipeURL + query;
        return requestLink;
    } // requestLink


    /**
     * This method will load products in list form.
     * It will show results from the term searched
     */
    public void loadProducts() {
        try {
            ArrayList<String> list3 = productSearch();
            if (list3.size() < 1) {
                String error2 = "failed";
            } else {
                Platform.runLater(() -> productList.getItems().clear());
                Platform.runLater(() -> productList.getItems().addAll(list3));
            } // if-else
            products = list3;
            Platform.runLater(() -> instruction.setText("Loading products, please wait."));
        } catch (Exception e2) {
            Platform.runLater(() -> alertError(e2, requestProductLink()));
        }

    } // loadStores

    /**
     * This method will search term for products and update the list.
     * @return a list of products
     */
    public ArrayList<String> productSearch() {
        ArrayList<String> productResults = new ArrayList<>();
        try {
            String productSearch = requestProductLink();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(productSearch)).build();
            HttpResponse response = HTTP_CLIENT.send(request, BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                Platform.runLater(() -> instruction.setText("Finding products failed, try again."));
                throw new IOException("Finding products failed, try again.");
            } // if
            String json = response.body().toString();
            ProductResponse productResponse = GSON.fromJson(json, ProductResponse.class);
            if (productResponse.count < 1) {
                Platform.runLater(() -> instruction.setText("No products found, try again."));
                throw new IllegalArgumentException("No products found, try again.");
            } // if
            if (productResults != null) {
                productResults.clear();
            } // if
            ProductResult productResult = productResponse.products[0];
            String[] split = productResult.categories.split(",\\s*");
            for (int j = 0; j <  split.length; j++) {
                productResults.add(split[j]);
            } // for

            return productResults;
        } catch (Exception e) {
            Platform.runLater(() -> alertError(e, requestProductLink()));
            return productResults;
        } // try-catch
    } // productSearch

/**
     * This method will return a url for the product search.
     * @return a string
     */
    public String requestProductLink() {
        String searchTerm = searchBox.getText();

        String productText = URLEncoder.encode(searchTerm, StandardCharsets.UTF_8);
        String query = "?categories_tags_en=" + productText;
        String requestLink = productURL + query;
        return requestLink;
    } // requestProductLink

    /**
     * This method will alert for errors if the user puts in an invalid search.
     * @param exception
     * @param link
     */
    public static void alertError(Throwable exception, String link) {
        TextArea errorText = new TextArea("URI: " + link + "n\n" + "Exception:" +
            exception.toString());
        errorText.setEditable(false);
        Alert alert = new Alert(AlertType.ERROR);
        alert.getDialogPane().setContent(errorText);
        alert.setResizable(true);
        alert.showAndWait();
    } // alertError

    /**
     * Creates a hyperlink label.
     * @param text
     * @param url
     * @return label
     */
    private Label createHyperlinkLabel(String text, String url) {
        Label newLabel = new Label(text);
        Platform.runLater(() -> {
            instruction.setText("Loading recipe please wait...");
            newLabel.setOnMouseClicked(e -> {
                Thread hyperlink = new Thread(() -> {
                    if (e.getClickCount() == 1) {
                        getHostServices().showDocument(url);
                    }
                });
                hyperlink.setDaemon(true);
                hyperlink.start();
            });
        });
        return newLabel;
    } // createHyperlinkLabel

} // ApiApp
