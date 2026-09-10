# sed script file to change the colour palate of a CSS file
# We have 5 shades from dark..light: v.dark,dark,medium,light,v.light
# s/@@top_body_background@@/ ~~light
# s/@@top_h1_font@@/ ~~v.dark
# s/@@treeitem_selected_border_left@@/ ~~dark
# s/@@treeitem_focus_border@@/ ~~medium
# s/@@treeitem_focus_background@@/ ~~medium
# s/@@treeitem_hover_background@@/ ~~dark
# s/@@treeitem_hover_border_left@@/ ~~dark
# s/@@top_select_background@@/ ~~medium
# s/@@top_select_font@@/ ~~v.dark
# s/@@top_option_background@@/ ~~light
# s/@@top_option_font@@/ ~~v.dark
# s/@@top_input_background@@/ ~~medium
# s/@@top_input_font@@/ ~~v.dark
# s/@@top_input_border@@/	~~medium
# s/@@top_header_background@@/ ~~medium
# s/@@banner_background@@/ ~~dark
# s/@@banner_h1_font@@/ ~~v.dark
# s/@@results_font@@/ ~~v.dark
# s/@@results_background@@/ ~~v.light
# s/@@results_table_row_odd_background@@/ ~~light 
# s/@@results_table_row_even_background@@/ ~~v.light
# s/@@results_table_th_background@@/ ~~light
# s/@@results_table_th_font@@/ ~~v.dark
# s/@@results_table_tr_th_background@@/ ~~light 
# s/@@results_table_tr_th_font@@/ ~~v.dark
# s/@@results_table_tr_td_border@@/ ~~medium
# s/@@ddtf-filtered_font@@/  ~~v.dark
# s/@@ddtf-filtered_background@@/ ~~v.light
# s/@@ddtf-filtered_select_background@@/ ~~light
# s/@@ddtf-filtered_select_font@@/ ~~v.dark
# s/@@ddtf-filtered_select_border@@/ ~~medium
# s/@@ddtf-processed_font@@/ ~~v.dark
# s/@@ddtf-process_background@@/ ~~medium
# s/@@menu-bar_box_shadow@@/ ~~dark
# s/@@menu-bar_background@@/ ~~light
# s/@@menu-bar_border@@/ ~~medium
# s/@@menu-bar_a_font@@/  ~~v.dark
# s/@@menu-bar_a_text_shadow@@/ ~~medium
# Colour Palatte : Ocean Color Palette 
# v.dark #18363E dark #5F97AA medium #2D5F6E light #3E88A5 v.light #93C4D1
# Now to apply:/
s/@@top_body_background@@/#3E88A5/
s/@@top_h1_font@@/#18363E/
s/@@treeitem_selected_border_left@@/#5F97AA/
s/@@treeitem_focus_border@@/#2D5F6E/
s/@@treeitem_focus_background@@/#2D5F6E/
s/@@treeitem_hover_background@@/#5F97AA/
s/@@treeitem_hover_border_left@@/#5F97AA/
s/@@top_select_background@@/#2D5F6E/
s/@@top_select_font@@/#18363E/
s/@@top_option_background@@/#3E88A5/
s/@@top_option_font@@/#18363E/
s/@@top_input_background@@/#2D5F6E/
s/@@top_input_font@@/#18363E/
s/@@top_input_border@@/#2D5F6E/
s/@@top_header_background@@/#2D5F6E/
s/@@banner_background@@/#5F97AA/
s/@@banner_h1_font@@/#18363E/
s/@@results_font@@/#18363E/
s/@@results_background@@/#93C4D1/
s/@@results_table_row_odd_background@@/#3E88A5 /
s/@@results_table_row_even_background@@/#93C4D1/
s/@@results_table_th_background@@/#3E88A5/
s/@@results_table_th_font@@/#18363E/
s/@@results_table_tr_th_background@@/#3E88A5 /
s/@@results_table_tr_th_font@@/#18363E/
s/@@results_table_tr_td_border@@/#2D5F6E/
s/@@ddtf-filtered_font@@/ #18363E/
s/@@ddtf-filtered_background@@/#93C4D1/
s/@@ddtf-filtered_select_background@@/#3E88A5/
s/@@ddtf-filtered_select_font@@/#18363E/
s/@@ddtf-filtered_select_border@@/#2D5F6E/
s/@@ddtf-processed_font@@/#18363E/
s/@@ddtf-process_background@@/#2D5F6E/
s/@@menu-bar_box_shadow@@/#5F97AA/
s/@@menu-bar_background@@/#3E88A5/
s/@@menu-bar_border@@/#2D5F6E/
s/@@menu-bar_a_font@@/ #18363E/
s/@@menu-bar_a_text_shadow@@/#2D5F6E/
    background-color: @@top_body_background@@; /* light/v.light */
    color: @@top_h1_font@@; /* dark/v.dark */
    border-left: 5px solid @@treeitem_selected_border_left@@;
    border-color: @@treeitem_focus_border@@; 
    background-color: @@treeitem_focus_background@@;
    background-color: @@treeitem_hover_background@@;
    border-left: 5px solid @@treeitem_hover_border_left@@;
    background-color: @@top_select_background@@; /* light/v.light */
    color: @@top_select_font@@; /* Default Select: Hooker's Green */
    background-color: @@top_option_background@@; /* light/v.light */
    color: @@top_option_font@@; /* dark/v.dark */
    background-color: @@top_input_background@@; /* light/v.light */
    color: @@top_input_font@@; /* dark/v.dark */
    border: 0px solid @@top_input_border@@; /* medium */	
    background-color: @@top_header_background@@; /* light/v.light */
    background-color: @@banner_background@@; /* light/v.light */
    color: @@banner_h1_font@@; /* dark/v.dark */
    color: @@results_font@@; 
    background-color: @@results_background@@; /* light */
    background-color: @@results_table_row_odd_background@@; /* medium */
    background-color: @@results_table_row_even_background@@; /* light */
    background-color: @@results_table_th_background@@; /* medium */
    color: @@results_table_th_font@@; /* v.dark/black */
    background-color:  @@results_table_tr_th_background@@; /* light/v.light */ 
    color: @@results_table_tr_th_font@@; /* v.dark/black */
    border: 0px solid @@results_table_tr_td_border@@;
    color: @@ddtf-filtered_font@@;
    background-color: @@ddtf-filtered_background@@; /* light/v.light */
    background-color: @@ddtf-filtered_select_background@@; /* light/v.light */
    color: @@ddtf-filtered_select_font@@; /* v.dark/black */
    border: 0px solid @@ddtf-filtered_select_border@@; /* medium */
    color: @@ddtf-processed_font@@; /* v.dark/black */
    background-color: @@ddtf-process_background@@; /* light/v.light */
    box-shadow: 2px 2px 3px @@menu-bar_box_shadow@@; /* medium */
    -webkit-box-shadow: 2px 2px 3px @@menu-bar_box_shadow@@; /* medium */
    -moz-box-shadow: 2px 2px 3px @@menu-bar_box_shadow@@; /* medium */
    background: @@menu-bar_background@@; /* light/v.light */
    border: double 1px @@menu-bar_border@@; /* medium */
    color: @@menu-bar_a_font@@; /* light grey */
    text-shadow: 2px 2px 3px @@menu-bar_a_text_shadow@@; /* medium */
