

(function($, undefined){
  //$("document").ready(function (){

    (function(){

      // ************** ????????????  *****************
var margin = {top: 20, right: 120, bottom: 20, left: 120},
 width = 1280 - margin.right - margin.left,
 height = 800 - margin.top - margin.bottom;
 
var i = 0;
var radius = 10;

var defaultNodeSize = 5.5;
var horizontalTreeOffset = 150;
var horizontalNodeOffset = horizontalTreeOffset - 10;
var horizontalNodeOffsetLeaf = horizontalTreeOffset + 10;

var tree = d3.layout.tree()
 .size([height, width]);

var diagonal = d3.svg.diagonal()
 .projection(function(d) { return [d.y, d.x]; });

var svg = d3.select("#viz3").append("svg")
 .attr("width", width + margin.right + margin.left)
 .attr("height", height + margin.top + margin.bottom)
  .append("g");
 .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

var color = d3.scale.category20();
  
update(root);

function update(source) { 

  // Compute the new tree layout.
  var nodes = tree.nodes(root).reverse(),
   links = tree.links(nodes);

  // Normalize for fixed-depth.
  nodes.forEach(function(d) { d.y = d.depth * 180; });

  // Declare the nodesâ€¦
  var node = svg.selectAll("g.node")
   .data(nodes, function(d) { return d.id || (d.id = ++i); });

  // Enter the nodes.
  var nodeEnter = node.enter().append("g")
   .attr("class", "node")
   .attr("transform", function(d) { 
    return "translate(" + d.y + "," + d.x + ")"; });

  nodeEnter.append("circle")
   .attr("r", radius)
   .style("fill", "#fff");

  // nodeEnter.append("text")
  //  .attr("x", function(d) { 
  //   return d.children || d._children ? -13 : 13; })
  //  .attr("dy", ".35em")
  //  .attr("text-anchor", function(d) { 
  //   return d.children || d._children ? "end" : "start"; })
  //  .text(function(d) { return d.id; })
  //  .style("fill-opacity", 1);

   // Use foreignObject to append HTML text in order to implement word wrapping
  nodeEnter.append("svg:foreignObject")
      .attr("width", "300")
      .attr("height", "60")
      .attr("x", -50)
      .attr("y", -15)
      .style("opacity", 0.8)
      .attr("text-anchor", "start")      
    .append("xhtml:body")
      .attr("xmlns", "http://www.w3.org/1999/xhtml")
      .html(function(d){ return "<a href=/projects/" + d.name + ">" + d.name + "</a>"; });

  // Declare the linksâ€¦
  var link = svg.selectAll("path.link")
   .data(links, function(d) { return d.target.id; });

  // Enter the links.
  link.enter().insert("path", "g")
   .attr("class", "link")
   .attr("d", diagonal)
   .style("stroke", function (d) { return 'black'; })


   
}


    
    })();


  //});
})(jQuery)