#powershell -noexit C:\RailsInstaller\Ruby1.9.3\bin\ruby.exe  '$(FULL_CURRENT_PATH)'
#cmd /K C:\RailsInstaller\Ruby1.9.3\bin\ruby.exe "$(FULL_CURRENT_PATH)" 
start = Time.now
path = File.path(__FILE__)[0... -1*File.basename(__FILE__).size]
path += '/' if not path.empty?

#
# Variables
#
file_input = path+'trace.txt'
file_output = path+'trace.sql'
@@DEBUG=false

#
# Patterns (Regex)
#
@@blank = /\A\s*\z/
@@binding_pattern = /:(\d+)(?=\D|\s|\z)/
@@bind_value_pattern = /Bind-(\d+) type=(\d+) length=(\d+) value=(.*)/
@@stmt_id = /COM Stmt=(.+)/


#
# Functions
#

def do_binding(_file,_sql)
	# check for bind elements.
	puts _sql if @@DEBUG
	if _sql =~ @@binding_pattern
		bindings_no = _sql.gsub(@@binding_pattern).count
		puts "#{bindings_no} bindings found:" if @@DEBUG
		bindings_no.times do |i|
			line = _file.gets
			puts i.to_s+": "+line if @@DEBUG
			# add binding to sql
			match = @@bind_value_pattern.match line
			bind = match[1]
			type = match[2]
			length = match[3]
			value = match[4]
			
			case type.to_i
				when 2 # Strings
					if value =~ @@blank
						value = '' 
					else
						value = "'"+value+"'"
					end
				when 19,8 # Numbers (int, float)
					if value =~ @@blank
						value = 'NULL'
					end
				else
					puts "unknown type: #{type} with value <#{value}>"
			end
			#puts line
			puts "\t\t'#{":"+bind}' ==> '#{value}' " if @@DEBUG
			_sql.gsub!(/#{":"+bind}(?=\D|\s|\z)/, value+'\1') # @@binding_pattern
		end
		puts "end with bindings => sql = #{_sql}" if @@DEBUG
	else
		puts "no binding found" if @@DEBUG
	end
	_sql
end

#
# Script Start
#

sql_stmts = []
file = File.open(file_input)

line = file.gets
while not line.nil? do
	if line and line =~ @@stmt_id
		
		# get SQL stmts
		match = @@stmt_id.match line
		sql = match[1]
		sql = sql.gsub(/\r/,"").gsub(/\n/,"") # remove break lines
		
		# check SQL Operation
		if sql =~ /\ASELECT/i
			print 'S'
			do_binding(file,sql) # ignore SQL SELECT statments
			#sql_stmts << do_binding(file,sql)
		elsif sql =~ /\AUPDATE/i
			print 'U'
			#puts ">>>> "+do_binding(file,sql)
			sql_stmts << do_binding(file,sql)
			#puts file.gets
		elsif sql  =~ /\AINSERT/i
			print 'I'
			sql_stmts << do_binding(file,sql)
		else
			print '?'
			puts "UN-PROCESSED SQL STMT >>>> "+sql
		end
	end
	line = file.gets
end

File.open(file_output, 'w') do |f|
	sql_stmts.each do |stmt|
		f.write(stmt + ";\n")
	end
end

finish = Time.now

puts "\n\n===== Results ===== "
puts "> #{File.open(file_input).lines.count} lines processed."
puts "> #{sql_stmts.count} SQL statements  extracted:"
puts "\t- #{sql_stmts.select{ |s| s =~ /\Aselect/i }.size} SQL SELECT statements  extracted. (ignored on purposed)"
puts "\t- #{sql_stmts.select{ |s| s =~ /\Ainsert/i }.size} SQL INSERT statements  extracted."
puts "\t- #{sql_stmts.select{ |s| s =~ /\Aupdate/i }.size} SQL UPDATE statements  extracted."
puts "> #{"%05.2f"%((finish-start)*1000)} ms time used."
puts "===== End ===== "
