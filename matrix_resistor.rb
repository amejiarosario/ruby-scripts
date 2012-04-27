INSERT_PATTERN = /insert\W+into\W+(\w+)\(([^)]*)\)\W*values\W*\(([^;]*)\)/i

@data_path = "matrix"
@data_file = "#{@data_path}/matrix_resistors.csv"
@query_file = "#{@data_path}/matrix_resistors.sql"
@output_file = "#{@data_path}/matrix_resistors_processed.sql"
DEBUG_LIMIT = 5

def csv_2_hash (file) 
	hash = nil
	header = nil
	times = 0
	File.open(file) do |f|
		while line = f.gets do
			if hash.nil?
				#header
				hash = {}
				header = line.split(",")
				header.each do |h|
					hash[h] = []
				end
			else
				line.split(",").each.with_index do |r,i|
					hash[header[i]] << r.to_s.strip.gsub(/[^a-zA-Z0-9\-\ %\.]/,"") # TODO refine
				end
			end
			times += 1
			break if times == DEBUG_LIMIT # TODO 
		end
	end
	hash
end

def main

	hash = csv_2_hash @data_file
	
	p "==============="
	puts hash.inspect
	p "==============="
	
	path = @query_file
	puts "reading... #{path}"
	
	File.open(path) do |f|
		# Get SQL stmt
		times = 0
		while line = f.gets do
			#puts line
			table = column_names = values = ""
			match = INSERT_PATTERN.match line
			if match
				table = match[1]
				column_names = match[2].split(",") unless match[2].nil?
				values = match[3].split(",") unless match[3].nil?
				
				# make a hash t with the key => value of the insert statement
				t={}
				t[:name] = table
				column_names.each.with_index do |c,i|
					t[c] = values[i].to_s.strip
				end
				puts "\n>> original: #{t.inspect}"
				
				#
				# replace the values if the hash of values have a column for it.
				#
				i = 0
				begin
					t.each_key do |k|
						unless hash[k].nil?
							t[k] = hash[k][i] || t[k]
						end
					end
					puts "\n>> changed: #{t.inspect}\n<<<<"
					i+=1
				end while hash[k] && hash[k][i]
				
				
				doit.times do |i|
					t.each_key {|k| t[k] = hash[k][i] || t[k] }
				end
				#
				#
				#
				
				times += 1
				
			else
				puts "DIDN'T match #{line}"
			end

			
			#puts "table=#{table}\ncolumns=#{column_names}\nvalues=#{values}\n\n"
			break if times ==  DEBUG_LIMIT
		end
=begin		
		File.open(@output_file,'w') do |out|
			s = "INSERT INTO #{table}(#{column_names.join(",")} VALUES(#{values.join("','")}))\n"
			puts s
			out.write(s)
		end
=end
	end
end


main
