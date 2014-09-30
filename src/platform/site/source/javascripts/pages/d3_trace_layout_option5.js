<style type="text/css">
  
  .node {
    cursor: pointer;
  }
 
  .overlay{
      background-color:#EEE;
      border: 1px solid red;
  }
   
  .node circle {
    fill: #fff;
    stroke: steelblue;
    stroke-width: 1.5px;
  }
   
  .node text {
    font-size:10px; 
    font-family:sans-serif;
  }
   
  .link {
    fill: none;
    stroke: #ccc;
    stroke-width: 1.5px;
  }
 
  .templink {
    fill: none;
    stroke: red;
    stroke-width: 3px;
  }
 
  .ghostCircle.show{
      display:block;
  }
 
  .ghostCircle, .activeDrag .ghostCircle{
       display: none;
  }
 
  </style>
  
(function($, undefined){
  //$("document").ready(function (){

    (function(){ 

    	var margin = {top: 20, right: 120, bottom: 20, left: 120};
    

    // Calculate total nodes, max label length
    var totalNodes = 0;
    var maxLabelLength = 0;

    // Misc. variables
    var i = 0;
    var duration = 750;
    var root;

     // size of the diagram
    var viewerWidth = $("#viz5").width()- margin.right - margin.left;
    var viewerHeight = $(document).height() - margin.top - margin.bottom; 



    var tree = d3.layout.tree()
        .size([viewerHeight, viewerWidth]); 

    // define a d3 diagonal projection for use by the node paths later on.
    var diagonal = d3.svg.diagonal()
        .projection(function(d) {
            return [d.y, d.x];
        });

    // A recursive helper function for performing some setup by walking through all nodes
 
    function visit(parent, visitFn, childrenFn) {
        if (!parent) return;
 
        visitFn(parent);
 
        var children = childrenFn(parent);
        if (children) {
            var count = children.length;
            for (var i = 0; i < count; i++) {
                visit(children[i], visitFn, childrenFn);
            }
        }
    }
 
    // Call visit function to establish maxLabelLength
    visit(treeData, function(d) {
        totalNodes++;
        maxLabelLength = Math.max(d.name.length, maxLabelLength);
 
    }, function(d) {
        return d.children && d.children.length > 0 ? d.children : null;
    }); 

    // define the baseSvg, attaching a class for styling
    var baseSvg = d3.select("#viz5").append("svg")
        .attr("width",  viewerWidth + margin.right + margin.left)
        .attr("height",  viewerHeight + margin.top + margin.bottom)
        .attr("class", "overlay");



    // Helper functions for collapsing and expanding nodes.
 
    function collapse(d) {
        if (d.children) {
            d._children = d.children;
            d._children.forEach(collapse);
            d.children = null;
        }
    }
 
    function expand(d) {
        if (d._children) {
            d.children = d._children;
            d.children.forEach(expand);
            d._children = null;
        }
    }

    var overCircle = function(d) {

        selectedNode = d;
        updateTempConnector();
    };
    var outCircle = function(d) {
        selectedNode = null;
        updateTempConnector();
    };
 
    // Function to center node when clicked/dropped so node doesn't get lost when collapsing/moving with large amount of children.
 
    function centerNode(source) { 
        scale = 0; //zoomListener.scale();
        x = -source.y0;
        y = -source.x0;
        x = viewerWidth / 2;
        y = viewerHeight / 2;
        d3.select('g').transition()
            .duration(duration)
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
            //.attr("transform", "translate(" + x + "," + y + ")scale(" + scale + ")");
        // zoomListener.scale(scale);
        // zoomListener.translate([x, y]);
    }
 
    // Toggle children function
 
    function toggleChildren(d) {
        if (d.children) {
            d._children = d.children;
            d.children = null;
        } else if (d._children) {
            d.children = d._children;
            d._children = null;
        }
        return d;
    }
 
    // Toggle children on click.
 
    function click(d) {
        if (d3.event.defaultPrevented) return; // click suppressed
        d = toggleChildren(d);
        update(d);
        centerNode(d);
    }

    function update(source) { 
        // Compute the new height, function counts total children of root node and sets tree height accordingly.
        // This prevents the layout looking squashed when new nodes are made visible or looking sparse when nodes //are removed
        // This makes the layout more consistent.
        var levelWidth = [1];
        var childCount = function(level, n) {
 
            if (n.children && n.children.length > 0) {
                if (levelWidth.length <= level + 1) levelWidth.push(0);
 
                levelWidth[level + 1] += n.children.length;
                n.children.forEach(function(d) {
                    childCount(level + 1, d);
                });
            }
        };

        //alert(levelWidth.length);
        childCount(0, root);
        //alert(levelWidth.length);
        var newHeight = d3.max(levelWidth) * 25; // 25 pixels per line  
        tree = tree.size([newHeight, viewerWidth]);

        //alert(newHeight);
 
        // Compute the new tree layout.
        var nodes = tree.nodes(root).reverse(),
            links = tree.links(nodes);


        // Set widths between levels based on maxLabelLength.
        nodes.forEach(function(d) {
            d.y = (d.depth * (maxLabelLength * 10)); //maxLabelLength * 10px
            // alternatively to keep a fixed scale one can set a fixed depth per level
            // Normalize for fixed-depth by commenting out below line
            // d.y = (d.depth * 500); //500px per level.            
        });
 
        // Update the nodes…
        node = svgGroup.selectAll("g.node")
            .data(nodes, function(d) { 
                return d.id || (d.id = ++i);
            });


 
        // Enter any new nodes at the parent's previous position.
        var nodeEnter = node.enter().append("g")            
            .attr("class", "node")
            .attr("transform", function(d) {  
                return "translate(" + source.y0 + "," + source.x0 + ")";
            });
                 
 
        nodeEnter.append("circle")
            .attr('class', 'nodeCircle')
            .attr("r", 0)
            .style("fill", function(d) {
                return d._children ? "lightsteelblue" : "#fff";
            });
 
        nodeEnter.append("text")
            .attr("x", function(d) {
                return d.children || d._children ? -10 : 10;
            })
            .attr("dy", ".35em")
            .attr('class', 'nodeText')
            .attr("text-anchor", function(d) {
                return d.children || d._children ? "end" : "start";
            })
            .text(function(d) {
                return d.name;
            })
            .style("fill-opacity", 0);
 
        // phantom node to give us mouseover in a radius around it
        nodeEnter.append("circle")
            .attr('class', 'ghostCircle')
            .attr("r", 30)
            .attr("opacity", 0.2) // change this to zero to hide the target area
        .style("fill", "red");
            // .attr('pointer-events', 'mouseover')
            // .on("mouseover", function(node) {
            //     overCircle(node);
            // })
            // .on("mouseout", function(node) {
            //     outCircle(node);
            // });
 
        // Update the text to reflect whether node has children or not.
        node.select('text')
            .attr("x", function(d) {
                return d.children || d._children ? -10 : 10;
            })
            .attr("text-anchor", function(d) {
                return d.children || d._children ? "end" : "start";
            })
            .text(function(d) {
                return d.name;
            });
 
        // Change the circle fill depending on whether it has children and is collapsed
        node.select("circle.nodeCircle")
            .attr("r", 4.5)
            .style("fill", function(d) {
                return d._children ? "lightsteelblue" : "#fff";
            });
 
        // Transition nodes to their new position.
        var nodeUpdate = node.transition()
            .duration(duration)
            .attr("transform", function(d) {
                return "translate(" + d.y + "," + d.x + ")";
            });
 
        // Fade the text in
        nodeUpdate.select("text")
            .style("fill-opacity", 1);
 
        // Transition exiting nodes to the parent's new position.
        var nodeExit = node.exit().transition()
            .duration(duration)
            .attr("transform", function(d) {
                return "translate(" + source.y + "," + source.x + ")";
            })
            .remove();
 
        // nodeExit.select("circle")
        //     .attr("r", 0);
 
        // nodeExit.select("text")
        //     .style("fill-opacity", 0);
 
        // Update the links…
        var link = svgGroup.selectAll("path.link")
            .data(links, function(d) {
                return d.target.id;
            });
 
        // Enter any new links at the parent's previous position.
        link.enter().insert("path", "g")
            .attr("class", "link")
            .attr("d", function(d) {
                var o = {
                    x: source.x0,
                    y: source.y0
                };
                return diagonal({
                    source: o,
                    target: o
                });
            });
 
        // Transition links to their new position.
        link.transition()
            .duration(duration)
            .attr("d", diagonal);
 
        // Transition exiting nodes to the parent's new position.
        link.exit().transition()
            .duration(duration)
            .attr("d", function(d) {
                var o = {
                    x: source.x,
                    y: source.y
                };
                return diagonal({
                    source: o,
                    target: o
                });
            })
            .remove();
 
        // Stash the old positions for transition.
        nodes.forEach(function(d) {
            d.x0 = d.x;
            d.y0 = d.y;
        });
    }



    // Append a group which holds all nodes and which the zoom Listener can act upon.
    var svgGroup = baseSvg.append("g").attr("transform", "translate(" + margin.left + "," + margin.top + ")");
 
    // Define the root
    root = treeData;
    root.x0 = viewerWidth / 2;
    root.y0 = 0;   
 
    // Layout the tree initially and center on the root node.
    update(root);    
    centerNode(root);

    }());

  //});
})(jQuery);