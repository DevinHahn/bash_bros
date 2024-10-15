$(document).ready(function() {
    const urlParams = new URLSearchParams(window.location.search);
    const movieId = urlParams.get('id');

    if (!movieId) {
        alert('Movie ID is missing!');
        return;
    }

    // Fetch movie data from the API
    $.ajax({
        url: 'api/single-movie?id=' + movieId, // Use the ID in the API endpoint
        method: 'GET',
        dataType: 'json',
        success: function(data) {
            console.log(data); // Log the full response for debugging

            if (data && !data.errorMessage) { // Check for errors in the response
                // Populate movie information
                $('#movie_info').text(data.title);

                // Create table row for movie details
                let rowHTML = `
                    <tr>
                        <td>${data.director}</td>
                        <td>${data.year}</td>
                        <td>${data.rating}</td>
                        <td>${data.genres}</td>
                        <td>${data.stars ? data.stars.split(',').map(starInfo => {
                    let starParts = starInfo.split(':'); // Split star info
                    if (starParts.length === 2) { // Check if we have both ID and name
                        let starId = starParts[0].trim(); // Get star ID
                        let starName = starParts[1].trim(); // Get star Name
                        // Create hyperlink to single star page
                        return '<a href="single-star.html?id=' + starId + '">' + starName + '</a>';
                    }
                    return ''; // Return empty string if format is not as expected
                }).join(', ') : 'No stars available'}</td>
                    </tr>
                `;
                $('#movie_table_body').append(rowHTML);
            } else {
                $('#movie_table_body').html('<tr><td colspan="5">Error retrieving movie information.</td></tr>');
            }
        },
        error: function() {
            $('#movie_table_body').html('<tr><td colspan="5">Error retrieving movie information.</td></tr>');
        }
    });
});