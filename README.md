# IMAS Project
Practical work for Introduction to Multi-agent Systems in MAI course

### Authors
- Santiago Bernal
- Pritomrit Bora
- Can Fan
- Mateusz Skibinski

Base code for the project was used from [mas-skeleton](https://github.com/jpahullo/mas-skeleton/tree/mas_2017_18)

### Setup
#### Eclipse
The external JARs (jade.jar and jadeExamples.jar) should be automatically imported into the classpath, if not, right click project > Properties > java Build Path > select Libraries tab > Add External JARs.

For running the project, go to Run > Run Configurations > Java Application > right click > New > set Main class to: jade.Boot > go to arguments tab > set Program arguments to: -gui -agents system:agent.SystemAgent 

After setting the run configuration you can run it from the button on the bottom of the dialog screen, or from the dropdown menu next to the "play" button in the eclipse window.
