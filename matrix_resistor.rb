# encoding: UTF-8

require 'set'
require 'pp'

INSERT_PATTERN = /insert\W+into\W+(\w+)\W*\(([^)]*)\)\W*values\W*\(([^;]*)\)/i

@data_path = "matrix"
@data_file = "#{@data_path}/ps_values.csv"
#@data_file = "#{@data_path}/matrix_resistors.csv"
@query_file = "#{@data_path}/matrix_resistors.sql"
@output_file = "#{@data_path}/matrix_resistors_processed.sql"
@changes_file = "#{@data_path}/matrix_resistors_changes.sql"

DEBUG_LIMIT = 3
TRACE = false
DEBUG = false

class String
	def csv_spliter(separator)
		# check for parenthesis
		# omit commas inside the parenthesis
		buffer = ""
		array = []
		parenthesis = 0
		
		it = self.each_char.to_enum
		begin
			while c = it.next
				case c
				when '('
					#parenthesis+=1
					while c
						buffer += c
						parenthesis +=1 if c == '('
						parenthesis -= 1 if c == ')'
						if parenthesis == 0 
							break
						else
							c = it.next
						end
					end
				when separator[0]
					array << buffer
					buffer = ""
				else
					buffer += c
				end
			end
		rescue StopIteration
			array << buffer
		end
		array
	end
	
	def no_invisibles
		self.gsub(/[^\ -}]/, '')
	end
	
end

def csv_2_hash (file) 
	hash = nil
	header = nil
	times = 0
	File.open(file) do |f|
		while line = f.gets do
			line = line.no_invisibles
			if hash.nil?
				#header
				hash = {}
				header = line.split(",")
				header.each do |h|
					hash[h] = []
				end
			else
				line.split(",").each.with_index do |r,i|
					hash[header[i]] << r.to_s.strip
				end
			end
			times += 1
			break if DEBUG and times > DEBUG_LIMIT # TODO 
		end
	end
	hash
end

def hash_2_sqlinsert (hash)
	"INSERT INTO #{hash[:table_name]} (#{hash.keys[1..-1].map{|k| k.to_s}.join(", ")}) VALUES (#{hash.values[1..-1].map{|v| v.to_s}.join(", ")});\n"
end

def main
	queries_set = Set.new
	hash = csv_2_hash @data_file
	
	p "===============" if DEBUG
	#puts hash.inspect if DEBUG
	pp hash if DEBUG
	p "===============" if DEBUG
	
	puts "reading... #{@query_file}"
	
	File.open(@changes_file,'w') do |changes|
	File.open(@query_file) do |f|
		
		changes.write "== List of changes ==\n"
		# Get SQL stmt
		times = 0
		while line = f.gets do
			#puts line
			line = line.no_invisibles
			table = column_names = values = ""
			match = INSERT_PATTERN.match line
			
			if match
				table = match[1]
				column_names = match[2].split(",") unless match[2].nil?
				values = match[3].csv_spliter(",") unless match[3].nil? # special spliter for commans inside parenthesis
				
				# make a hash t with the key => value of the insert statement
				t={}
				t[:table_name] = table
				column_names.each.with_index do |c,i|
					t[c] = values[i].to_s.strip.no_invisibles
				end
				puts "\n>> original: #{t.inspect}" if TRACE
				changes.write "> Original:\n\tSQL: #{line}\n\tValues: #{t.inspect}\n"
				
				replaced_columns = Set.new
				
				#
				# replace the values if the hash of values have a column for it.
				#
				hash.values[0].count.times do |i|
					t.each_key do |k|
						unless hash[k].nil?
							t[k] = "'#{hash[k][i].no_invisibles}'" if hash[k][i]
							puts "<#{k}> updated to <#{t[k]}>" if TRACE
							#changes.write "<#{k}> updated to <#{t[k]}>\n"
							replaced_columns.add(k)
						end
					end
					puts "\n>> changed: #{t.inspect}\n<<<<" if DEBUG
					queries_set.add "#{hash_2_sqlinsert(t).no_invisibles} -- case 1"
				end
				#
				#
				#
				
				changes.write "\tColumns changed: #{replaced_columns.to_a.join(", ")} \n" 
				
				times += 1
			elsif line =~ /select/i && line =~ /insert/i
				changes.write "> Original:\n\tSQL: #{line}\n\tValues: n/a\n"
				hash.values[0].count.times do |i|
					line = line.gsub(/ALTEST1/i, hash['INV_ITEM_ID'][i])
					line = line.gsub(/VICHP/i, hash['BUSINESS_UNIT'][i])
					line = line.gsub(/CLASSCODE-916/, hash['INV_ITEM_ID'][i])
					queries_set.add "#{line.no_invisibles} -- case 2"
				end
				changes.write "\tValues replaced: 'ALTEST1' => `INV_ITEM_ID`, 'VICHP' => `BUSINESS_UNIT`, 'CLASSCODE-916' => `INV_ITEM_ID` \n"
			else
				puts "\n*** NOT processed: \n#{line}"
				changes.write "\n*** NOT processed: \n#{line}\n\n"
			end

			#puts "table=#{table}\ncolumns=#{column_names}\nvalues=#{values}\n\n"
			break if DEBUG and times >  DEBUG_LIMIT
		end
	end
	end
	
	# Output file
	File.open(@output_file,'w') do |out|
		queries_set.each do |v|
			out.write "#{v}\n"
		end
	end
	
end


main
