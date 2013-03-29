$(document).ready(function(){

  getHiddenFieldsValues();
  sortFilters();
  addCheckboxesFilters();
  setOnChange();
  budgetFilteringSetUp();

});

function hasTrue(array) {
  return ($.inArray(true, array)!= -1);
}

function budgetFilteringSetUp() {
  var divsToCheck = $('input[name=status][type="hidden"]').parent('div');
  var max = 0;
  var min = 0;
  $( "input[name=budget][type='hidden']" ).each(function(i, input){
    if(max < input.value){
      max = +(input.value);
    }
  });

  $( "#slider-vertical" ).slider({
    orientation: "horizontal",
    range: true,
    min: min,
    max: max,
    step : (Math.round(max / 100) * 100)/100,
    values: [min,max],
    slide: function( event, ui ) {
      $( "#amount" ).html( ("£"+ui.values[0]).replace(/(\d)(?=(?:\d{3})+$)/g, "$1,")+" - "+ ("£"+ui.values[1]).replace(/(\d)(?=(?:\d{3})+$)/g, "$1,"));
    },
    change: function( event, ui ) {
      if (!$('input[type=checkbox]').is(':checked')) {
        $(".search-result").each(function(i, div) {
          if ( ($(this).children("input[name='budget']").val() <= ui.values[1]) && 
               ($(this).children("input[name='budget']").val() >= ui.values[0]) ) {
            $(this).show();
          } else {
            $(this).hide();
          }
        });
      } else {
        filter(divsToCheck);
      }
      displayResultsAmount();
    }
  });

  $( "#amount" ).html( ("£"+min).replace(/(\d)(?=(?:\d{3})+$)/g, "$1,") +" - "+("£"+max).replace(/(\d)(?=(?:\d{3})+$)/g, "$1,"));
}

function filter(divsToCheck) {
  if( !$('input[type=checkbox]').is(':checked') && ($('#slider-vertical').slider("option", "values")[1] == $('#slider-vertical').slider("option", "max")) ) {
    $('.search-result').css("display", "inline");
    displayResultsAmount();
    return false;
  }

  divsToCheck.each(function(i, div) {
    var anythingFound = false;    
    var budget = 0;

    var hasStatus = new Array();
    var isStatusGroupActive = false;
    $('input:checked[name=status]').each(function(i, checkboxess) {
      anythingFound = true;
      isStatusGroupActive = true;
      if ($(div).children('input[value*="'+checkboxess.value+'"][name="status"]').length > 0) {
        hasStatus.push(true);
      }
    });
     
    var hasOrganisations = new Array();    
    var isOrganisationsGroupActive = false;    
    $('input:checked[name=organizations]').each(function(i, checkboxess) {
      anythingFound = true;
      isOrganisationsGroupActive = true;
      if ($(div).children('input[value*="'+checkboxess.value+'"][name="organizations"]').length > 0) {
        hasOrganisations.push(true);
      }
    });

    var hasSectors = new Array();
    var isSectorsGroupActive = false;
    $('input:checked[name=sectors]').each(function(i, checkboxess) {
      anythingFound = true;
      isSectorsGroupActive = true;
      if ($(div).children('input[value*="'+checkboxess.value+'"][name="sectors"]').length > 0) {
        hasSectors.push(true);
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

    var divBudget = +($(div).children('input[name="budget"]').val());
    var min = +($('#slider-vertical').slider("option", "values")[0]);
    var max = +($('#slider-vertical').slider("option", "values")[1]);
    if (divBudget > max || divBudget < min) {
      show.push(false);
    } 

    if ($.inArray(false, show)!= -1) {
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
var Countries = new Array();
var Regions = new Array();
var Organizations = new Array();
var Sectors = new Array();
var SectorGroups = new Array();

function unique(array){
  return $.grep(array,function(el,index) {
    return index == $.inArray(el,array);
  });
}

function SortByName(a, b) {
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

function splitAndAssign(string, outputArray) {
  var splited = string.split('#');
  $.each(splited, function(i,val) {
    if ( val.length > 0) { 
      if ($.inArray(val, outputArray)==-1) {
        outputArray.push(val); 
      }
    }
  });
}

function getHiddenFieldsValues(){
  $(".search-result input[name=status]").each(function() {
    splitAndAssign($(this).attr("value"),Status)
  });

  $(".search-result input[name=sectors]").each(function() {
    splitAndAssign($(this).attr("value"),Sectors)
  });

  $(".search-result input[name=organizations]").each(function() {
    splitAndAssign($(this).attr("value"),Organizations)
  });
}

function addCheckboxesFilters() {
  $.each( Status, function( key, value ) {
    $("div[name=status]").append(createInputCheckbox('status', value));
  });

  $.each( Sectors, function( key, value ) {
    $("div[name=sectors]").append(createInputCheckbox('sectors', value));
  });

  $.each( Organizations, function( key, value ) {
    $("div[name=organizations]").append(createInputCheckbox('organizations', value));
  });
}

function createInputCheckbox(name, value) {
  return "&nbsp;<input type='checkbox' name='"+name+"' value='"+value+"'>&nbsp;"+value+"</input><br>";
}
