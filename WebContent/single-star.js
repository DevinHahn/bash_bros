/**
 * Handles the data returned by the API, reads the jsonObject and populates data into HTML elements
 * @param resultData jsonObject
 */
function handleResult(resultData) {
    console.log("handleResult: populating star info from resultData");

    // Check if we have the star information
    if (!resultData || !resultData["id"]) {
        console.log("No star data found in the result.");
        jQuery("#star_info").text("No star information available.");
        return;
    }

    // Populate the star information
    jQuery("#star_info").html(
        "<p>Star Name: " + resultData["name"] + "</p>" +
        "<p>Birth Year: " + (resultData["birthYear"] ? resultData["birthYear"] : "N/A") + "</p>"
    );

    // Populate the movie information
    let movieListElement = jQuery("#movie_table_body");
    movieListElement.empty(); // Clear previous entries

    if (resultData["movies"] && resultData["movieIds"]) {
        let movies = resultData["movies"].split(", "); // Split the concatenated movie titles
        let movieIds = resultData["movieIds"].split(", "); // Split the concatenated movie IDs

        for (let i = 0; i < movies.length; i++) {
            // Create a clickable link for each movie title
            let rowHTML = "<tr><td><a href='single-movie.html?id=" + movieIds[i] + "'>" + movies[i] + "</a></td></tr>";
            movieListElement.append(rowHTML);
        }
    } else {
        movieListElement.append("<tr><td>No movie information available.</td></tr>");
    }
}

// Extract star ID from the URL
let starId = new URLSearchParams(window.location.search).get("id");

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/single-star?id=" + starId, // Setting request URL with star ID
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully
});