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
# s/@@menu-bar_a_font@@/  ~~v.dark/black
# s/@@menu-bar_a_text_shadow@@/ ~~medium
# Ocean Color Palette: 	       v.dark    #18363E         dark #5F97AA                     medium #2D5F6E          light #3E88A5      v.light #93C4D1
# Underwater Colour Palette:| Dark Blue #0c2766 | Blue #3d5285 | Medium Blue #6d7da3 | Gray Blue #9ea9c2 | Gray #ced4e0
# Now to apply:/
s/@@top_body_background@@/\#9ea9c2/g 
s/@@top_h1_font@@/black/g 
s/@@treeitem_selected_border_left@@/\#6d7da3/g
s/@@treeitem_focus_border@@/\#6d7da3/g 
s/@@treeitem_focus_background@@/\#ced4e0/g
s/@@treeitem_hover_background@@/\#9ea9c2/g
s/@@treeitem_hover_border_left@@/\#6d7da3/g
s/@@top_select_background@@/\#ced4e0/g 
s/@@top_select_font@@/black/g 
s/@@top_option_background@@/\#ced4e0/g 
s/@@top_option_font@@/black/g 
s/@@top_input_background@@/\#ced4e0/g 
s/@@top_input_font@@/black/g 
s/@@top_input_border@@/\#6d7da3/g 
s/@@top_header_background@@/\#9ea9c2/g 
s/@@banner_background@@/\#ced4e0/g 
s/@@banner_h1_font@@/black/g 
s/@@results_font@@/black/g 
s/@@results_background@@/\#9ea9c2/g 
s/@@results_table_row_odd_background@@/\#6d7da3/g 
s/@@results_table_row_even_background@@/\#9ea9c2/g 
s/@@results_table_th_background@@/\#6d7da3/g 
s/@@results_table_th_font@@/black/g 
s/@@results_table_tr_th_background@@/\#6d7da3/g 
s/@@results_table_tr_th_font@@/black/g 
s/@@results_table_tr_td_border@@/\#6d7da3/g
s/@@ddtf-filtered_font@@/black/g
s/@@ddtf-filtered_background@@/\#ced4e0/g 
s/@@ddtf-filtered_select_background@@/\#9ea9c2/g 
s/@@ddtf-filtered_select_font@@/black/g 
s/@@ddtf-filtered_select_border@@/\#6d7da3/g 
s/@@ddtf-processed_font@@/black/g 
s/@@ddtf-process_background@@/\#ced4e0/g 
s/@@menu-bar_box_shadow@@/\#6d7da3/g 
s/@@menu-bar_background@@/\#9ea9c2/g 
s/@@menu-bar_border@@/\#6d7da3/g 
s/@@menu-bar_a_font@@/black/g 
s/@@menu-bar_a_text_shadow@@/\#6d7da3/g 
