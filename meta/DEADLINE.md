# Deadline

Modify this file to satisfy a submission requirement related to the project
deadline. Please keep this file organized using Markdown. If you click on
this file in your GitHub repository website, then you will see that the
Markdown is transformed into nice-looking HTML.

## Part 1.1: App Description

This is a recipe app meant to aid users in finding recipes and getting ideas to make meals. It uses 2 APIs: Edamam Recipe API and OpenFoodFacts Search API. The Edamam Recipe API provides a database of recipes from the search query that is then presented to the user in list form. The user can click on the specified recipe they want to browse and that will open a webpage to the recipe's url to where they can see the ingredients and instructions. The OpenFoodFacts Search API allows users to find products that match with the user's search term. It provides a list of related items that users can take inspiration from if they don't want to use a recipe.

https://github.com/jth24477/cs1302-api-app

## Part 1.2: APIs


### Edamam Recipe Search API

```
https://api.edamam.com/api/recipes/v2?type=public&q=chicken&app_id=35b6401b&app_key=%2041191a205a196f9830c5d43ffd55a9d8%09
```

### OpenFoodFacts Search API

```
https://world.openfoodfacts.net/api/v2/search?categories_tags_en=chicken.
```

## Part 2: New

From this project I have learned more about the JavaFX GUI. I wanted to be able to place the information from the JSON objects parsed into a near list format. I learned how to do so with ListView and adding labels to the lists so that it is clickable for the user. Personally, I am a food lover and I love to cook, so I was able to combine my interests in making food into this app.

## Part 3: Retrospect

One thing I wished I could change from this project in the beginning is using a different format. I wanted to orginally use a StackView format but opted for a BorderPane format. I also want to be able to make this app more aesthetically pleasing, but didn't have the time to learn how to. I wanted to add another API that implemented a grocery store aspect where users can directing add ingredients from the recipes into their carts.
