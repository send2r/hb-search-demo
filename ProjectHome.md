# Overview #
This is a java swing based project to show case hibernate search features.
The project is based on maven, so all you need to do is to check out the project, install maven and make sure the maven executable - 'mvn' is set in your PATH. Also, ensure that your JAVA\_HOME is set to appropriate JDK installation.
Modern IDEs (Netbeans, IDEA, eclipse) allow you to import a maven project and work seamlessly.
Most interesting part is that, this project uses embedded derby. So, you do not require to set up any database. Just run target

`mvn clean install`

and

`mvn exec:java`

# Technologies #
  * hibernate /hibernate search
  * JPA
  * maven
  * Swing

# Demo overview #
This demo is based on a list of Songs with some fields such as Artist, Title, Notes etc.
Up on running the demo, you can populate the test table using a button in the user interface. Data is loaded using text file stored at hbsearch\src\main\resources\data.txt.
Once data is loaded and indexed(which runs after data load), you may switch over to the
Query tab to test out various types of queries.

The main class to look for is DemoHelper.java, which does the actual hibernate search( only a very few lines). Rest of the code is mostly Swing/util stuff.

### Queries in action ###
Multifield ( search across all fields)
![http://farm6.staticflickr.com/5521/9323201192_a6cdf6e82e_c.jpg](http://farm6.staticflickr.com/5521/9323201192_a6cdf6e82e_c.jpg)

Wildcard (regular expression style search)
![http://farm8.staticflickr.com/7304/9323200578_fb6fb91bb3_c.jpg](http://farm8.staticflickr.com/7304/9323200578_fb6fb91bb3_c.jpg)

Fuzzy(based on "Levenshtein distance" style search)
![http://farm4.staticflickr.com/3730/9320408163_c3609f5b90_c.jpg](http://farm4.staticflickr.com/3730/9320408163_c3609f5b90_c.jpg)