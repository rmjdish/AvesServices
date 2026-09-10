#!/usr/bin/env python3
"""
@brief Command-line interface for CSV transformation tool
@file cli.py
"""
import sys
import argparse
from transform import GenTrans, TransException

def main():
    """Command-line entry point"""
    parser = argparse.ArgumentParser(
        description="Transform CSV files between long and wide formats"
    )
    parser.add_argument(
        "conf", 
        type=str, 
        help="Path to the JSON transform configuration file."
    )
    parser.add_argument(
        "--verbose", "-v",
        action="store_true",
        help="Enable verbose output"
    )
    
    args = parser.parse_args()
    
    # Create transformer instance
    trans = GenTrans(args.conf)
    
    try:
        # Read configuration
        trans.read_json()
        
        if args.verbose:
            trans.print_state()
        
        # Get configuration parameters
        direction = trans.get_direction()
        
        # Execute transformation
        if direction == "wide":
            trans.check_input()
            # trans.go_wide()  # Uncomment when implemented
        elif direction == "long":
            trans.go_long()
        else:
            print(f"Error: Unknown direction '{direction}' in configuration")
            sys.exit(1)
            
        print(f"Transformation complete. Output written to {trans.get_output_file()}")
        
    except TransException as err:
        print(f"Error: {err}")
        sys.exit(1)
    except FileNotFoundError as err:
        print(f"File error: {err}")
        sys.exit(1)
    except Exception as err:
        print(f"Unexpected error: {err}")
        sys.exit(1)

if __name__ == "__main__":
    main()














    
