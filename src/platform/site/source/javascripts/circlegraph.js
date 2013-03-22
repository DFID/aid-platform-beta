
CircleGraph = function (container) {

  this.drawGlobalProjectsGraph = function(labels) {
    this.clearContainer();

    // calculate central circle's dimensions
    var circleRadius = 0.13 * this.width;
    var verticalShift = 0.05 * this.width;

    this.drawCentralCircle(circleRadius, verticalShift, labels);
  }

  this.drawRegionalProjectsGraph = function(labels, regionsData) {
    this.clearContainer();

    // calculate central circle's dimensions
    var innerCircleRadius = 0.13 * this.width;
    var outerCircleRadius = 0.22 * this.width;

    this.drawCentralCircle(innerCircleRadius, 0.2*innerCircleRadius, labels);

    var maxBudget = d3.max(regionsData.regionalProjects.map(function(project) { return project.budget; }));
    var minBudget = d3.min(regionsData.regionalProjects.map(function(project) { return project.budget; }));
      
    var sateliteContainerW = 0.17 * this.width;
    var sateliteContainerH = 0.10 * this.width;

    var minSateliteCircleR = 0.10 * sateliteContainerH;
    var maxSateliteCircleR = 0.20 * sateliteContainerH;

    var scale = d3.scale.linear();
    scale.domain([minBudget, maxBudget]);
    scale.range([minSateliteCircleR, maxSateliteCircleR]);

    plots = regionsData.regionalProjects.length;
    increase = Math.PI * 2 / plots,
    angle = 15,
    x = 0,
    y = 0;
    for(var i = 0; i < plots; i++) {
      var sateliteCircleR = scale(regionsData.regionalProjects[i].budget)
      var p = new SateliteCircle(container, 'element_' + i, regionsData.regionalProjects[i],
                                 sateliteContainerW, sateliteContainerH, sateliteCircleR);
      x = outerCircleRadius * Math.cos(angle) + this.width / 2 + 0.13*innerCircleRadius;
      y = outerCircleRadius * Math.sin(angle) + this.height / 2 ;//- 0.2*innerCircleRadius;
      p.position(x, y);

      (function(){
        var code = p.getData().code;
        $(p.getElement()).click(function(){
          window.location= "/regions/" + code + "/projects";
        })
      })()

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

  this.drawOuterCentralCircle = function(radius) {
    var circleShiftX = this.width / 2 - radius;
    var circleShiftY = this.height / 2 - radius;

    var g = d3.select(container.selector + ' svg')
                .append("g")
                .attr("transform", "translate(" + circleShiftX + ", " + circleShiftY + ")");

    g.append("circle")
        .style("stroke", "gray")
        .style("fill", "transparent")
        .style("z-index", "-1")
        .attr("r", radius)
        .attr("cx", radius)
        .attr("cy", radius);
  }
};

SateliteCircle = function (container, selector, data, w, h, r) {

  this.data = data;
  this.position = function( x, y ) {
    this.elm.style.left = (x - w / 2) + 'px';
    this.elm.style.top = (y - h / 2) + 'px';
    this.x = x;
    this.y = y;
  };
  
  this.elm = document.createElement('div');
  this.elm.style.cursor = 'hand';
  this.elm.style.position = 'absolute';  
  this.elm.id = selector;
  this.elm.style.width = w + 'px';
  this.elm.style.height = h + 'px';
  this.elm.style.overflow = 'visible';
  this.width = w;
  this.height = h;
  container.append(this.elm);

  var g = d3.select('#' + this.elm.id)
              .append("svg")
              .append("g");

  g.append("circle")
      .style("fill", "#008270")
      .style("opacity", "0.7")
      .style("cursor", "hand")
      .attr("r", r)
      .attr("cx", w / 2)
      .attr("cy", r);

  var textSize = 0.6 * r;
  if (textSize > 7) {    
    g.append("text")
      .attr("text-anchor", "middle")
      .attr("fill", "#FFF")            
      .attr("transform", "translate(" +  (w / 2) + ", " + 1.2 * r + ")")
      .style("font-size", textSize + "px")
      .style("line-height", "1.1em")
      .style("cursor", "hand")
      .append("tspan")
         .text(format_million_stg(data.budget));
  }
  
  g.append("text")
      .attr("text-anchor", "middle")
      .attr("transform", "translate(" + w / 2 + ", " + (2.6 * r) + ")")
      .style("font-size", 10 + "px")      
      .style("line-height", "1.2em")
      .append("tspan")
         .text(data.region)         
         .attr("x", "0")
         .attr("dy", "0");
};

SateliteCircle.prototype.getElement = function(){
  return this.elm;
}

SateliteCircle.prototype.getData = function(){
  return this.data;
}

format_million_stg = function(amt) {
  return '\u00A3' + Math.floor(amt / 1000000) + 'm';
}
