$(document).ready(function(){

getHiddenFieldsValues();
sortFilters();
addCheckboxesFilters();
setOnChange();
budgetFilteringSetUp();
});

function budgetInRange(budget){
  var budgetMin = $('input[type=number][name=min]');
  var budgetMax = $('input[type=number][name=max]');
    var inRange = false;
    if(budgetMax.val() == ""){ // min empty
        inRange = budgetMin.val() < budget;
    } else if(budgetMin.val() == ""){ // max empty
        inRange = budget < budgetMax.val();
    } else { // both filled
        inRange = budgetMin.val() < budget && budget < budgetMax.val();d
    } 
    return inRange;
}

//console.log(budgetInRange());

function hasTrue(array){
    if($.inArray(true, array)!= -1){
        return true;
    } else {
        return false;
    }
}

function budgetFilteringSetUp() {
var divsToCheck = $('input[name=status][type="hidden"]').parent('div');
var max = 0;
$( "input[name=budget][type='hidden']" ).each(function(i, input){
    if(max < input.value){
      max = +(input.value);
    } 
});

$( "#slider-vertical" ).slider({
orientation: "horizontal",
range: "min",
min: 0,
max: max,
step : (Math.round(max / 100) * 100)/100,
value: max,
slide: function( event, ui ) {
$( "#amount" ).html( ("£"+ui.value).replace(/(\d)(?=(?:\d{3})+$)/g, "$1,") );
},
change: function( event, ui ) {
  if(!$('input[type=checkbox]').is(':checked')){
    $(".search-result").each(function(i, div){
        if($(this).children("input[name='budget']").val() > ui.value){
        $(this).hide();
        } else {
        $(this).show();
        }
    });
  }else{
    filter(divsToCheck);
  }
  displayResultsAmount();
  }
});
$( "#amount" ).html( ("£"+max).replace(/(\d)(?=(?:\d{3})+$)/g, "$1,") );
}




function filter(divsToCheck){

if(!$('input[type=checkbox]').is(':checked')&&($('#slider-vertical').slider("option", "value") == $('#slider-vertical').slider("option", "max"))) {
    $('.search-result').css("display", "inline");
    displayResultsAmount();
    return false;
}

divsToCheck.each(function(i, div){

            var anythingFound = false;
            var hasStatus = new Array();
            var isStatusGroupActive = false;
            var budget = 0;
            $('input:checked[name=status]').each(function(i, checkboxess){
                anythingFound = true;
                isStatusGroupActive = true;
                if($(div).children('input[value*="'+checkboxess.value+'"][name="status"]').length > 0){
                    hasStatus.push(true);
                }
             });
             
            var hasOrganisations = new Array();    
            var isOrganisationsGroupActive = false;
            $('input:checked[name=organizations]').each(function(i, checkboxess){
                anythingFound = true;
                isOrganisationsGroupActive = true;
                if($(div).children('input[value*="'+checkboxess.value+'"][name="organizations"]').length > 0){
                    hasOrganisations.push(true);
                }
             });

             var hasSectors = new Array();
             var isSectorsGroupActive = false;
              $('input:checked[name=sectors]').each(function(i, checkboxess){
                anythingFound = true;
                isSectorsGroupActive = true;
                if($(div).children('input[value*="'+checkboxess.value+'"][name="sectors"]').length > 0){
                    hasSectors.push(true);
                }
              });
             
              var hasCountries = new Array(); 
              var isCountriesGroupActive = false;
              $('input:checked[name=countries]').each(function(i, checkboxess){
                anythingFound = true;
                isCountriesGroupActive = true;
                if($(div).children('input[value*="'+checkboxess.value+'"][name="countries"]').length > 0){
                    hasCountries.push(true);
                }
              });
             
             var hasRegions = new Array(); 
             var isRegionsGroupActive = false;
              $('input:checked[name=regions]').each(function(i, checkboxess){
                anythingFound = true;
                isRegionsGroupActive = true;
                if($(div).children('input[value*="'+checkboxess.value+'"][name="regions"]').length > 0){
                    hasRegions.push(true);
                }
              });
             
            var show = new Array();
            if(isStatusGroupActive){
                show.push(hasTrue(hasStatus));
            } 
            if(isOrganisationsGroupActive){
                show.push(hasTrue(hasOrganisations));
            }
            if(isSectorsGroupActive){
                 show.push(hasTrue(hasSectors));
            }
            if(isCountriesGroupActive){
                 show.push(hasTrue(hasCountries));
            } 
            if(isRegionsGroupActive){
                show.push(hasTrue(hasRegions));
            }

            if(+($(div).children('input[name="budget"]').val()) > +($('#slider-vertical').slider("option", "value"))){
                show.push(false);
            } 

            if($.inArray(false, show)!= -1){
                $(div).css("display", "none");
                } else {
                $(div).css("display", "inline");
                }

            if(!anythingFound){
                $('.search-result').css("display", "none"); //show none cuz nothing found
            }
        });
displayResultsAmount();
}

function setOnChange(){
  var divsToCheck = $('input[name=status][type="hidden"]').parent('div');
  $('input[type=checkbox]').change(function() {
    filter(divsToCheck);
  });
}
 

function displayResultsAmount(){
  $('span[name=afterFilteringAmount]').html(($(".search-result").length - $(".search-result:hidden").length) + " of ");
}

var Status = new Array();
var Sectors = new Array();
var Countries = new Array();
var Regions = new Array();
var Organizations = new Array();

function unique(array){
    return $.grep(array,function(el,index){
        return index == $.inArray(el,array);
    });
}

function SortByName(a, b){
  var aName = a.toLowerCase();
  var bName = b.toLowerCase(); 
  return ((aName < bName) ? -1 : ((aName > bName) ? 1 : 0));
}

function sortFilters(){
     Status = Status.sort(SortByName);
     Sectors = Sectors.sort(SortByName);
     Countries = Countries.sort(SortByName);
     Regions = Regions.sort(SortByName);
     Organizations = Organizations.sort(SortByName);
}

function splitAndAssign(string, outputArray){
    var splited = string.split('#')
      $.each(splited, function(i,val){
          if( val.length > 0){ 
            if($.inArray(val, outputArray)==-1){
                outputArray.push(val); 
            }
          }
      });
}

function getHiddenFieldsValues(){
    $(".search-result input[name=status]").each(function() {
    splitAndAssign($(this).attr("value"),Status)
    });

    $(".search-result input[name=organizations]").each(function() {
    splitAndAssign($(this).attr("value"),Organizations)
    });

    $(".search-result input[name=countries]").each(function() {
    splitAndAssign($(this).attr("value"),Countries)
    });

    $(".search-result input[name=sectors]").each(function() {
    splitAndAssign($(this).attr("value"),Sectors)
    });

    $(".search-result input[name=regions]").each(function() {
    splitAndAssign($(this).attr("value"),Regions)
    });
}

function addCheckboxesFilters(){
    $.each( Status, function( key, value ) {
    $("div[name=status]").append("&nbsp;<input type='checkbox' name='status' value='"+value+"'>&nbsp;"+value+"<br>")
    });
    $.each( Sectors, function( key, value ) {
    $("div[name=sectors]").append("&nbsp;<input type='checkbox' name='sectors'  value='"+value+"'>&nbsp;"+value+"<br>")
    });
    $.each( Countries, function( key, value ) {
    $("div[name=countries]").append("&nbsp;<input type='checkbox' name='countries' value='"+value+"'>&nbsp;"+value+"<br>")
    });
    $.each( Regions, function( key, value ) {
    $("div[name=regions]").append("&nbsp;<input type='checkbox' name='regions'  value='"+value+"'>&nbsp;"+value+"<br>")
    });
    $.each( Organizations, function( key, value ) {
    $("div[name=organizations]").append("&nbsp;<input type='checkbox' name='organizations'  value='"+value+"'>&nbsp;"+value+"<br>")
    });
}

