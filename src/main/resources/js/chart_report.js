/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

window.everit = window.everit || {};
everit.jttp = everit.jttp || {};
everit.jttp.chart_report = everit.jttp.chart_report || {};

(function(jttp, jQuery) {

  jQuery(document).ready(function() {
    var jttpUserPicker = new AJS.CheckboxMultiSelect({
      element: jQuery('#userPicker'),
      submitInputVal: true,
      maxInlineResultsDisplayed: 100,
      ajaxOptions: {
        url: AJS.contextPath() + "/rest/api/2/groupuserpicker",
        data: {
          showAvatar: true
        },
        query: true,
        formatResponse: function(items) {
          var users = [];
          if (items.users.total) {
            users = _.map(items.users.users, function(item) {
              return new AJS.ItemDescriptor({
                highlighted: true,
                html: item.html,
                icon: item.avatarUrl,
                label: item.displayName,
                value: item.key
              });
            });
          }
          var resultItems = [].concat(users);
          return [new AJS.GroupDescriptor({ items: resultItems })];
        }
      }
    });

    jttpUserPicker._setDescriptorSelection = function(descriptor, $input) {
      var descriptValue = descriptor.value();
      if (!descriptor.selected()) {
        this.selectItem(descriptor);
        $input.attr("checked", "checked");
      } else {
        this.unselectItem(descriptor);
        $input.removeAttr("checked");
      }
    };
    updatePickerButtonText('#userPicker', '#userPickerButton', AJS.I18n.getText("jtrp.report.chart.user"));
    jQuery('#userPicker').on("change unselect", function() {
      updatePickerButtonText('#userPicker', '#userPickerButton', AJS.I18n.getText("jtrp.report.chart.user"));
    });
  });
  function updatePickerButtonText(picker, button, defaultText, noneText, noneValue) {
    //FIND and decide none checked
    var newButtonText = "";
    jQuery(picker).find("option:selected").each(function() {
      var optionText = jQuery(this).text();
      if (newButtonText === '') {
        newButtonText = optionText;
      }
      else {
        newButtonText = newButtonText + "," + optionText;
     }
    });
    if (newButtonText === '') {
      newButtonText = defaultText;
    }
    else if (newButtonText.length > 16) {
      newButtonText = newButtonText.substring(0, 12) + "...";
    }
    jQuery(button).text(newButtonText);
  };

  jttp.beforeSubmitChartReport = function() {
    var start = '';
    try{
      var dateFrom = jQuery('#dateFrom').val();
      var dateFromMil = Date.parseDate(dateFrom, jttp.options.dateFormat);
      if(dateFrom != dateFromMil.print(jttp.options.dateFormat)){
        showErrorMessage("error_message_label_df");
        return false;
      }
      start = everit.jttp.report_common_scripts.timeZoneCorrection(dateFromMil);
    } catch(err) {
      showErrorMessage("error_message_label_df");
      return false;
    }
    var end = '';
    try{
      var dateTo = jQuery('#dateTo').val();
      var dateToMil =  Date.parseDate(dateTo, jttp.options.dateFormat);
      if(dateTo != dateToMil.print(jttp.options.dateFormat)){
        showErrorMessage("error_message_label_dt");
        return false;
      }
      end = everit.jttp.report_common_scripts.timeZoneCorrection(dateToMil);
    } catch(err) {
      showErrorMessage("error_message_label_dt");
      return false;
    }
    var selectedUsers = jQuery('#userPicker').val() || [];
    var users = '';
    for(var i= 0; i < selectedUsers.length; i++) {
      users+= '&users=' + selectedUsers[i];
    }
    var groupby = jQuery('#groupByDropdownButton').attr('data-jtrp-value');
    
    var display = jQuery('#displayDropdownButton').attr('data-jtrp-value');
    
    jQuery.ajax({
      url : AJS.params.baseURL + "/rest/jttp-rest/1/worklog-summary-gadget/getWorklogSummaryDataForChart?display=" + display
           + "&groupBy=" + groupby
           + users
           + "&start=" + start
           + "&end=" + end,
      async : false
    }).done(function (data) {
      var div = '';
      var options = null;
      if(display == 'PieChart'){
        div = '<div id="jtrp-gadget-chart" class="chart" style="margin: 10px;  height: 550px; width: 980px"></div>'
        options = {
          series: {
            pie: { 
              show: true,
              radius: 1,
            }
          },
          grid: {
            hoverable: true
          },
          legend: {
            show: true,
          },
          tooltip: true,
          tooltipOpts: {
            content: function(label,x,y){
              return  "<strong>" + AJS.I18n.getText("jttp.gadget.time_spent") + "</strong><br><strong>"
                   + (+(Math.round(y + "e+2")  + "e-2"))+ "h"  +  "</strong>"
            },
            shifts: {
              x: 20,
              y: 0
            },
            defaultTheme: true
          }
        }
      } else {
        div = '<div id="jtrp-gadget-chart" class="chart" style="float: left; height: 550px; width: 980px"></div>'
            + '<div id="jttp-legendholder" style="padding-top: 5px;"></div>';
        options = {
          series: { 
            lines: {
              fill: true,
            },
            bars: {
              show: true,
              barWidth: 0.8,
              align: 'center',
              fill: 1,
              lineWidth: 0,
              labelWidth: 10
            }
          },
          xaxis: {
            mode: "categories",
            tickLength: 0,
            autoscaleMargin: 0.1
          },
          yaxis: {
            tickFormatter: function (v, axis) {
              return v + " h";
            }
          },
          grid: { 
            hoverable: true,
            clickable: true
          },
          legend: {
            show: true,
            container: '#jttp-legendholder',
            labelFormatter: function(label, series) {
              // series is the series object for the label
              return label;
            }
          }
        }
      }
      jQuery('#chart-panel').html(div);
      var plot = AJS.$.plot( AJS.$("#jtrp-gadget-chart"), data, options);
      
      AJS.$("#jttp-gadget-chart").bind("plothover", function (event, pos, item) {
        if (item) {
          if ((previousLabel != item.series.label) || (previousPoint != item.dataIndex)) {
            previousPoint = item.dataIndex;
            previousLabel = item.series.label;
            AJS.$("#jttp-gadget-tooltip").remove();

            var x = item.datapoint[0];
            var y = item.datapoint[1];
            var z = item.datapoint[2];

            var color = item.series.color;

            var currentHeight=y-z;
            AJS.$('<div id="jttp-gadget-tooltip">' 
                  + "<strong>" + AJS.I18n.getText("jttp.gadget.time_spent") + "</strong><br><strong>" + (+(Math.round(currentHeight + "e+2")  + "e-2"))+ "h"  +  "</strong>"
                  + '</div>').css({
                position: 'absolute',
                display: 'none',
                top: item.pageY - 40,
                left: item.pageX,
            })
              .addClass('gadget_tooltip')
              .appendTo("body").fadeIn(200);
          } else {
            AJS.$("#jttp-gadget-tooltip").remove();
            previousPoint = null;
          }
        }
      });
    });
    everit.jttp.report_common_scripts.jttpSearchOnClick('Chart');
  }
  
  function showErrorMessage(message_key){
    jQuery('#error_message label').hide();
    var errorMessageLabel = jQuery('#'+message_key);
    errorMessageLabel.show();
    var errorMessage = jQuery('#error_message');
    errorMessage.show();
  }
  
  jttp.changeGroupBy = function(obj) {
    var $obj = jQuery(obj);
    var $groupByDropdownButton = jQuery('#groupByDropdownButton');
    var newValue = $obj.attr('data-jtrp-value');
    var label = $obj.attr('data-jtrp-label');
    $groupByDropdownButton.attr('data-jtrp-value', newValue);
    $groupByDropdownButton.text(label);
  }
  
  jttp.changeDisplay = function(obj) {
    var $obj = jQuery(obj);
    var $displayDropdownButton = jQuery('#displayDropdownButton');
    var newValue = $obj.attr('data-jtrp-value');
    var label = $obj.attr('data-jtrp-label');
    $displayDropdownButton.attr('data-jtrp-value', newValue);
    $displayDropdownButton.text(label);
  }
})(everit.jttp.chart_report, AJS.$);
