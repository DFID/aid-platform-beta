
CircleGraph = function (container) {

  this.drawGlobalProjectsGraph = function(labels) {
    this.clearContainer();

    // calculate central circle's dimensions
    var circleRadius = 0.13 * this.width;
    var verticalShift = 0.05 * this.width;

    this.drawCentralCircle(circleRadius, verticalShift, labels);
  }

  this.drawRegionalProjectsGraph = function(labels) {
    this.clearContainer();

    // calculate central circle's dimensions
    var innerCircleRadius = 0.13 * this.width;
    var outerCircleRadius = 0.20 * this.width;

    this.drawCentralCircle(innerCircleRadius, 0, labels);
    //this.drawOuterCentralCircle(outerCircleRadius);
    

    plots = 26;
    increase = Math.PI * 2 / plots,
    angle = 0,
    x = 0,
    y = 0;

    for(var i = 0; i < plots; i++) {
      var p = new Plot(container, 'element_' + i);
      x = outerCircleRadius * Math.cos(angle) + this.width / 2 + 0.015 * this.width;
      y = outerCircleRadius * Math.sin(angle) + this.height / 2;
      p.position(x, y);
      angle += increase;
    }
  }

  this.clearContainer = function() {
    // first remove the graph if it exist inside the container 
    // this prevents drawing multiple graphs while resising window
    $(container.selector + ' svg').remove();
    $(container.selector + ' div').remove();

    // update the graph's container dimensions
    this.width = container.width();
    this.height = 0.55 * this.width; // original width to height ratio
  }

  this.drawOuterCentralCircle = function(radius) {
    var circleShiftX = this.width / 2 - radius;
    var circleShiftY = this.height / 2 - radius;

    var g = d3.select(container.selector + ' svg')
                .append("g")
                .attr("transform", "translate(" + circleShiftX + ", " + circleShiftY + ")");

    g.append("circle")
        .style("stroke", "gray")
        .style("fill", "transparent")
        .attr("r", radius)
        .attr("cx", radius - 0.5 * 0.33 * 0.045 * this.width)
        .attr("cy", radius - 0.5 * 0.33 * 0.045 * this.width);
  }

  this.drawCentralCircle = function(r, shiftY, labels) {

    var circleShiftX = this.width / 2 - r;
    var circleShiftY = this.height / 2 - r - shiftY; // shiftY - additional shift, when circle is not centered on Y axis

    var headerTextSize = 0.14 * r;
    var amountTextSize = 2 * headerTextSize;

    var g = d3.select(container.selector)
                .append("svg")
                .append("g")
                .attr("transform", "translate(" + circleShiftX + ", " + circleShiftY + ")");

    g.append("circle")
        .style("fill", "#008270")
        .style("opacity", "0.7")
        .attr("r", r)
        .attr("cx", r)
        .attr("cy", r);
    g.append("text")
        .attr("text-anchor", "middle")
        .attr("fill", "#FFF")            
        .attr("transform", "translate(" + r + ", " + r + ")")
        .style("font-size", headerTextSize + "px")
        .style("line-height", "1.1em")
        .append("tspan")
           .text(labels.header)
           .attr("x", "0")
           .attr("dy", "-0.3em")
        .append("tspan")
           .text(labels.amount)
           .attr("x", "0")
           .attr("dy", "1em")
           .style("font-size", amountTextSize + "px");
  }
};

Plot = function (container, selector) {

  var w = 0.045 * container.width();
  var h = w;

  this.position = function( x, y ) {
    var xoffset = arguments[2] ? 0 : this.width / 2;
    var yoffset = arguments[2] ? 0 : this.height / 2;
    this.elm.style.left = (x - xoffset) + 'px';
    this.elm.style.top = (y - yoffset) + 'px';
    this.x = x;
    this.y = y;
  };
  
  this.elm = document.createElement('div');
  this.elm.style.position = 'absolute';  
  this.elm.id = selector;
  this.elm.style.width = w + 'px';
  this.elm.style.height = h + 'px';
  //this.elm.style.border = '1px solid black';
  this.width = w;
  this.height = h;
  container.append(this.elm);

  var r = 0.33 * w;

  var g = d3.select('#' + this.elm.id)
              .append("svg")
              .append("g");

  g.append("circle")
      .style("fill", "#008270")
      .style("opacity", "0.7")
      .attr("r", r)
      .attr("cx", w / 2)
      .attr("cy", r);
  
};