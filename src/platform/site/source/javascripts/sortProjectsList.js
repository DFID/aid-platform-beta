(function($, undefined){
    $(document).ready(function(){

        $('#sortProjBudgAsc').click(function(e){
                        sortByBudget('asc');
                        e.preventDefault();
                    });

        $('#sortProjBudgDsc').click(function(e){
            sortByBudget('dsc');
            e.preventDefault();
        });

        $('#sortProjTitleAsc').click(function(e){
            sortByTitle('asc');
            e.preventDefault();
        });

        $('#sortProjTitleDsc').click(function(e){
            sortByTitle('dsc');
            e.preventDefault();
        });

        alert('hi');

  });

  function sortByBudget(order){
      alert('hi');
      var containerDiv = $('#search-results');
      var childResultDivs = containerDiv.children('.search-result').get();

      childResultDivs.sort(function (a, b){
          var compA = new Number( $(a).find('.sort-budget').val());
          var compB = new Number( $(b).find('.sort-budget').val());

          if(order=='asc')
              return (compA < compB) ? -1 : (compA > compB) ? 1 : 0;
          else
              return (compA < compB) ? 1 : (compA > compB) ? -1 : 0;
      })

      $.each(childResultDivs, function(idx, item){containerDiv.append(item);});
  }

 function sortByTitle(order){
      alert('hi');
      var containerDiv = $('#search-results');
      var childResultDivs = containerDiv.children('.search-result').get();

      childResultDivs.sort(function (a, b){

          var compA = $(a).find('.sort-title').val().toString();
          var compB = $(b).find('.sort-title').val().toString();

          if(order=='asc')
              return (compA.toLowerCase() < compB.toLowerCase()) ? -1 : (compA.toLowerCase() > compB.toLowerCase()) ? 1 : 0;
          else
              return (compA.toLowerCase() < compB.toLowerCase()) ? 1 : (compA.toLowerCase() > compB.toLowerCase()) ? -1 : 0;
      })

      $.each(childResultDivs, function(idx, item){containerDiv.append(item);});
  }
}