#Renamer

Small Java app for formatting the file names of TV shows in the following format:
`{SHOW_NAME} - s{SEASON}e{EPISODE} - {EPISODE_TITLE}`

**Example:** `Game of Thrones - s01e01 - Winter Is Coming`

It will process all files in the current directory, but will skip all files not matching the reference file (see below) or matching files with an invalid name. A file name has to include a season and an episode number, optionally separated by `e` or `x`. The only exception to this are David Letterman, The Daily Show, and Jimmy Fallon, which uses dates as episode identifiers (these three shows are currently hardcoded into the app).

##Source
The app uses [IMDb](http://imdb.com/) to provide episode titles and requires a text file containing references to show name, simplified show name (lower cased and dots replaced with spaces) and the IMDb ID.
The text file should be online and the URL inserted on line 11. The format of each line in the file:
`{SHOW_NAME}#{SIMPLIFIED_TITLE}#{IMDB_ID}`

**Example:** `Game of Thrones#game of thrones#tt0944947`

##Usage
Compile the .java file:
`javac Renamer.java`

Create an executable .jar file (optional):
`jar -cvfe Renamer.jar Renamer *.class`

##Development
If you have any suggestions or modifications, feel free to add an issue or submit a pull request.