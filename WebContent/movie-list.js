/**
 * Handles the data returned by the API, reads the jsonObject, and populates data into HTML elements
 * @param resultData jsonObject
 */
function handleMovieResult(resultData) {
    console.log("handleMovieResult: populating movie table from resultData");

    // Log the entire resultData to see what it contains
    console.log(resultData); // This will print the entire JSON response from the API

    // Populate the movie table
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Iterate through resultData, no more than 20 entries
    for (let i = 0; i < Math.min(20, resultData.length); i++) {
        // Concatenate the HTML tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<td><a href='single-movie.html?id=" + resultData[i]["id"] + "'>" + resultData[i]["title"] + "</a></td>";
        rowHTML += "<td>" + resultData[i]["year"] + "</td>";
        rowHTML += "<td>" + resultData[i]["director"] + "</td>";
        rowHTML += "<td>" + resultData[i]["rating"] + "</td>";
        rowHTML += "<td>" + resultData[i]["genres"] + "</td>";

        // Handle stars linking
        let stars = resultData[i]["stars"].split(',');
        stars = stars.slice(0, 3);  // Taking only the first three stars

        let starsHTML = stars.map(starInfo => {
            let [starId, starName] = starInfo.split(':');
            return '<a href="single-star.html?id=' + starId.trim() + '">' + starName + '</a>';
        }).join(', ');

        rowHTML += "<td>" + starsHTML + "</td>";
        rowHTML += "</tr>";

        // Append the row created to the table body
        movieTableBodyElement.append(rowHTML);
    }
}

/**
 * Once this .js is loaded, the following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers the success callback function handleMovieResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movie-list", // Full URL to the API
    success: (resultData) => handleMovieResult(resultData), // Callback function to handle data returned successfully
    error: (jqXHR, textStatus, errorThrown) => { // Error handling
        console.error("Error occurred while fetching movie list:", textStatus, errorThrown);
        console.error("Response Text:", jqXHR.responseText);

        // Optional: Display an error message to the user
        alert("An error occurred while fetching the movie list. Please try again later.");
    }
});