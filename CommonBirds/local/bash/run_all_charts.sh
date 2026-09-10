#!/bin/bash
# shell script to run all the plots 
python metaplot.py table:metabmlcs09_imp_1 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:metabmlcs09_org_1 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:medication6369x dcol2018
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:metabmlcs09_imp_2 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:metabmlcs09_org_2 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:postal22 dcol2023
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:echo09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:imtc09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:metabolomics09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:metabolomics15 dcol2018
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:de89d dcol2005
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:medicalconditions15x dcol2018
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:dxa09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:blood_urine09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:med_nurse_bnf09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:med_postal_bnf09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:capi99a dcol2005
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:med_nurse_all09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:imt09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:ecg09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:pwa09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:bnf99 derived2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:pwv09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:meds7799 derived2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:angina_claudication_postal09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:y92 dcol1982
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:boneshape09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:cvdevents14x dcol2018
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:htwtbmi derived2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:y64 dcol1982
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:med_postal_all09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:medications15x dcol2018
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:thyroidgp09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:cortisol09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:physical_performance09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:econom_circum09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:predicted_lungfunction89_15 derived2020
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:int_recoded derived2020
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:physical_perfrm15x dcol2018
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:b14 dcol1982
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:diabetes09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:blood_pressure09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:cvd_bp derived2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:hrv09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:angina_claudication_nurse09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:cvd_problems09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:igf09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:y65 dcol1982
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:b15 dcol1982
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:bodysize_recodes derived2020
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:echo_derived09 derived2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:actiheart_derived09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:y96 dcol1982
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:lungfunction15 derived2020
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:blood_pressure15x dcol2018
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:antidepr_upto1999 derived2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:other_health_probs09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:cognition09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:aceiii15x dcol2018
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:cognition15x dcol2018
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:testosterone09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:b18 dcol1982
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:b20 dcol1982
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:med_postal_antihyper09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:med_nurse_resp09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:med_postal_resp09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:epilepsy99 derived2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:socioecon15 dcol2018
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:cvd_events09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:epilepsy82 derived2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:cog89 derived2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:cognition1999 derived2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:med_postal_osteo09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:grip1999 derived2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:telomere09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:y97 dcol1982
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:lungfunction8999 derived2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:bodysize_recodes derived2020
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:cognition_3 derived2020
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:cognition_2 derived2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:cvdtestq09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:med_nurse_antihyper09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:angina82 derived2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:angina89 derived2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:angina99 derived2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:respiratory_problems09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:respiratoryproblems14x dcol2018
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:cancer09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:bodysize_recodes derived2020
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:d03x dcol1982
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:response_status09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:lung_function09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:o347 dcol1982
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:y60 dcol1982
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:y10 dcol1982
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:stroke_bp09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:lungfunc09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:fits_epilepsy09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:d03 dcol1982
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:homocysteine09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:v2recodes_d190516 derived2020
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

python metaplot.py table:physical_capability09 dcol2010
mysql --defaults-extra-file=~/.mysql/mysql.cnf  < outputs/load_csv_plots.sql
sleep 2
rm outputs/*

