Map {
  background-color: #b8dee6;
}

#countries {
  ::outline {
    line-color: #777777;
    line-width: 2;
    line-join: round;
  }
  polygon-fill: #c3bebe;
}

#rivers {
  [zoom >= 4]{
    line-color: #b8dee6;
    line-opacity: 0.5;
    line-width: 2;
  }
}

#lakes {
  [zoom >= 5]{
  	polygon-fill: #b8dee6;
  }
}


#places {
  [zoom < 4]{marker-width: 0}
  [zoom = 4]{
    marker-width: 3;
    marker-fill: #000;
    text-name: [NAME];
    text-face-name: "Arial Bold";
    text-size: 10;
    text-dx: 2;
  	text-dy: -2;
  	text-fill: #000000;
    text-opacity: 1;
  	text-allow-overlap: false;
  }
  [zoom = 5]{
    marker-width: 7;
    marker-fill: #000;
    text-name: [NAME];
    text-face-name: "Arial Bold";
    text-size: 10;
    text-dx: 4;
  	text-dy: -4;
  	text-fill: #000000;
  	text-allow-overlap: true;
  }
  [zoom >= 6]{
    marker-width: 10;
    marker-fill: #000;
    text-name: [NAME];
    text-face-name: "Arial Bold";
    text-size: 10;
    text-dx: 5;
  	text-dy: -5;
  	text-fill: #000000;
  	text-allow-overlap: true;
  }  
}