INSERT_PATTERN = /insert\W+into\W+(\w+)\(([^)]*)\)\W*values\W*\(([^)]*)\)/i

@data_path = "matrix"
@data_file = "matrix_resistors.csv"
@query_file = "matrix_resistors.sql"

def main 
	File.open(@query_file) do |f|
		# Get SQL stmt
		line = f.gets
		match = INSERT_PATTERN.match line
		table = match[1]
		column_names = match[2].split(",")
		values = match[3].split(",")
		
		File.open('matrix_resistors_processed.sql','w') do |out|
			out.write("INSERT INTO #{table}(#{column_names.join(",")} VALUES(#{values.join("','")}))\n")
		end
	end
end


main